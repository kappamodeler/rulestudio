(function($){

  $.flashCommunication = {
    events : {
      READY : 'ready',
      RESIZE : 'resize',
      SET_SELECTED_RULES: 'setSelectedRules',
      BULK_UPDATE_RULES: 'bulkUpdateRules',
      MARK_MODIFIED: 'markModified',
      SELECT_RULE: 'selectRule',
      DESELECT_RULE: 'deselectRule'
    },

    bindEvents : function(){
      $.gateway.listen('contactMapEmbed', $.flashCommunication.events.READY, onContactMapReady);
      $.gateway.listen('contactMapEmbed', $.flashCommunication.events.BULK_UPDATE_RULES, onBulkUpdateRules);
      $.gateway.listen('contactMapEmbed', $.flashCommunication.events.SELECT_RULE, onSelectRule);
      $.gateway.listen('contactMapEmbed', $.flashCommunication.events.DESELECT_RULE, onDeselectRule);
      $.gateway.listen('ModelEditor', $.flashCommunication.events.RESIZE, onModelEditorResize);
    },


    obsolete : {
      markModified: function() {
        if ($('#ModelEditorEmbed')[0]) {
          $.gateway.send('ModelEditorEmbed', $.flashCommunication.events.MARK_MODIFIED);
        } else {
          console.log("Attempt to mark the model modified over external interface failed");
        }
      },

      clearAllRules: function() {
        ruleListTransaction(function() {
            for(var rule_id in rules){
              deselectRule(rule_id);
            }
            updateRuleCounts();
        });
        return false;
      },

      selectRule: function(id) {
        selectRule(id);
      },

      deselectRule: function(id) {
        deselectRule(id);
      },

      selectAllRules: function() {
        ruleListTransaction(function() {
            for(var rule_id in rules){
              selectRule(rule_id);
            }
            updateRuleCounts();
        });
        return false;
      },

      removeRuleFromList: function(item){
        id = $(item).parent().attr("rule_id");
        deselectRule(id);
        updateRuleCounts();
      },

      updateChapterListCounts: function() {
        updateChapterListCounts();
      }
    }
  };


  /***********************************************************************************************
  **
  **  Event Handlers
  **
  ************************************************************************************************/
  function onContactMapReady(sender, message) {
    contactMap = sender;

    if (contactMap !== null) {
      var rules = "";
      $('.overlay li.rule').each(function(i) {
        if (rules.length > 0) { rules += ','; }
        rules += $(this).attr("rule_id");
      });

      // Set rule selection in SWF
      $.gateway.send('contactMapEmbed', $.flashCommunication.events.SET_SELECTED_RULES, {rules: rules});
    } else {
      console.log("Error: Unable to access contact map swf to set selected rules.");
    }

  }


  /**
   * onBulkUpdateRules(rules) is called by the Contact Map swf
   * and adds or removes rules from the selection list on the left side
   * of the rule picker lightbox
   */
  function onBulkUpdateRules(sender, message) {
    rules = message.rulesChanged;
    ruleListTransaction(function() {
        for (var i=0; i < rules.length; i++) {
          if (rules[i].selected) {
            selectRule(rules[i].id, rules[i].name);
          } else {
            deselectRule(rules[i].id);
          }
        }
    });
  }

  function onSelectRule(sender, message) {
    var id = message.id;
    var displayName = message.name;
    selectRule(id, displayName, rate_editable);
  }

  function onDeselectRule(sender, message) {
    var id = message.id;
    deselectRule(id);
  }

  function onModelEditorResize(sender, message) {
      $('#ModelEditorContainer').height(message.height);
  }
  /***********************************************************************************************
  **
  **  Private Functions
  **
  ************************************************************************************************/


  /**
   * Adds a rule from the selection list on the left side of the Rule Picker lightbox
   */
  function selectRule(id, displayName, rate_editable) {
    rate_editable = (rate_editable === null) ? true : rate_editable;

    if( $(".rules-by-chapter li.rule[rule_id="+id+"]").length === 0 ){

      if(!displayName){
          displayName = rules[id].name;
      }
      if(rate_editable)
      {
        var forward = $("<div><input type='text' value='"+
                        rules[id].forward_rate +
                        "' name='rules[" + id + "][forward]'" +
                        "' class='forward_rate_field' />" + rules[id].forward_rate_units+ "</div>");

        var backward = $("<div><input type='text' value='"+
                        rules[id].backward_rate +
                        "' name='rules[" + id + "][backward]'" +
                        "' class='backward_rate_field' />" + rules[id].backward_rate_units + "</div>");
      } else {
        var forward = $("<span>" + rules[id].forward_rate + "</span><input type='hidden' name='rules[" + id + "][forward]' value='" + rules[id].forward_rate + "'/>");
        var backward = $("<span>" + rules[id].backward_rate + "</span><input type='hidden' name='rules[" + id + "][backward]' value='" + rules[id].backward_rate + "'/>");
      }

      var deleteMe = $.button({
        buttonClass : 'right deleteRule white',
        leftIcon : 'delete-icon-16 white'
      });

      forward = $("<div class='rateA'></div>").append(forward);
      backward = $("<div class='rateB'></div>").append(backward);

      rates = $("<div class='rateIndicator clearfix'>").append(forward).append(backward);

      kappa = $("<div class='rule-kappa'>").append(rules[id].kappa);
      ruleText = $("<div class='rule-text left'>").append(displayName).append(kappa);

      // If the rule is a refinement, it's 'container_id' will be the rule it is a
      // refinement of. This gets the container_id of that rule(that id is of the chapter we are looking for)
      if(typeof(rules[rules[id].container_id]) != "undefined"){
        chapter_id = rules[rules[id]['container_id']]['container_id'];
      } else {
        chapter_id = rules[id]['container_id'];
      }

      query_string = '.rules-by-chapter ul.rules[id='+chapter_id+']';
      $(query_string).append( $("<li class='rule clearfix' id='rules["+id+"]' rule_id='" + id + "'></li>").
              append(deleteMe).
              append(rates).
              append(ruleText) );

      $("#cb_rules\\[" + id + "\\]").attr("checked",true);
    }

    if (contactMap !== null) {
      try {
        $.gateway.send('contactMapEmbed', $.flashCommunication.events.SELECT_RULE, {ruleId: id});
      } catch(ex) {}
    }

    if (ruleListTransactionCount === 0) {
        updateRuleCounts();
    }
  }

  /**
   * Removes a rule from the selection list on the left side of the Rule Picker lightbox
   */
  function deselectRule(id) {
    $("#cb_rules\\[" + id + "\\]").attr("checked",false);
    $(".rules-by-chapter li.rule[rule_id=" + id + "]").remove();

    if (contactMap !== null) {
      try {
          $.gateway.send('contactMapEmbed', $.flashCommunication.events.DESELECT_RULE, {ruleId: id});
        } catch(ex) {}
    } else {
      console.log("There was an error accessing the Contact Map external interface");
    }

    if (ruleListTransactionCount === 0) {
        updateRuleCounts();
    }
  }


  function ruleListTransaction(updateFunction) {
      try {
        ruleListTransactionCount++;
        updateFunction();
      } finally {
          if (--ruleListTransactionCount === 0) {
              ruleListTransactionCount = 0;
              updateRuleCounts();
          }
      }
  }

  function updateRuleCounts(){
    $("#rule_count").html($(".rules-by-chapter ul ul > li").size()); // the first ul is of chapters, we want to count the things in the chapters

    updateChapterListCounts();
  }

  function updateChapterListCounts() {
    $(".rules-list-chapter .ui-block-slider-titlebar-subtitle").each(function(){
        $(this).html('(' + $(this).parent().next().find('ul > li').size() + ')');
    });
  }

  var contactMap;
  var ruleListTransactionCount = 0;
})(jQuery);
