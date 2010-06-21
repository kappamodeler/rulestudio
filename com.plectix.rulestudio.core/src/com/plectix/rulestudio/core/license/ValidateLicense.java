/**
 * 
 */
package com.plectix.rulestudio.core.license;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.progress.UIJob;
import org.osgi.framework.Version;

import com.plectix.rulestudio.core.Activator;

/**
 * @author bill
 *
 */
public class ValidateLicense {
	
	// milliseconds in a week 
	private final static long ONE_WEEK = 1000 * 60 * 60 * 24 * 7;
	
	private static String username = null;
	private static String apiKey = null;
	private static UIJob loadLicense = null;
	private static boolean doneRenewal = false;

	private static String encryptedLicenseData = null;
	
    // private static final byte[] keyBytes = new byte[] {-64,-57,-57,76,114,-108,8,-17,-113,-50,-3,83,-115,-89,-89,-8};

	public static void setLicense(String license) {
    	encryptedLicenseData = license;
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		Version version = Activator.getDefault().getVersion();
		store.setValue("celludate_version", version.toString());
    	getLicense();
    }
    
    private static void getLicense() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		username = store.getString("cellucidate_email");
		apiKey = store.getString("cellucidate_api_key");
		boolean doRenewal = false;
		if (encryptedLicenseData == null || encryptedLicenseData.length() == 0) {
			encryptedLicenseData = store.getString("cellucidate_license");
			if (encryptedLicenseData.length() > 0) {
				doRenewal = true;		// check for renewal if we already have a license
			}
		}
		if (username.length() == 0 || apiKey.length() == 0 || encryptedLicenseData.length() == 0)
			throw new ValidateException();
    }
	
	private final static String toByte = "0123456789abcdef";
	
	private static byte[] stringToKey(String string) {
		byte[] result = new byte[string.length()/2];
		for (int i = 0, j = 0; i < result.length; ++i, j += 2) {
			int upper = toByte.indexOf(string.charAt(j));
			int lower = toByte.indexOf(string.charAt(j+1));
			result[i] = (byte)(upper << 4 | lower);	
		}
		return result;
	}
	
	public synchronized static void showError() {
		if (loadLicense != null)
			return;
		
		loadLicense = new UIJob("Load License.") {

			@Override
			public IStatus runInUIThread(IProgressMonitor monitor) {
				Display display = Display.getCurrent();
				if (display == null)
					display = Display.getDefault();
				Shell parent = display.getActiveShell();
				if (parent == null) {
					parent = new Shell(display);
				}
				
				GetLicenseWizard wizard = new GetLicenseWizard(encryptedLicenseData != null);					
				WizardDialog dlg = new WizardDialog(parent, (IWizard)wizard);	
				dlg.open();
				loadLicense = null;
				return Status.OK_STATUS;
			}
			
		};
		loadLicense.schedule();
	}

}
