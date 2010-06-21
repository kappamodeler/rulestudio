package com.plectix.kappa.editors.test;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

public class KappaMarker implements IMarker {
	private Map<String, Object> atList = null;
	private String type;
	private static long idNum = 0L;
	private long id = 0;
	
	public KappaMarker(String type) {
		this.type = type;
		synchronized(KappaMarker.class) {
			id = idNum++;
		}
	}

	@Override
	public void delete() throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean exists() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getAttribute(String attributeName) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getAttribute(String attributeName, int defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getAttribute(String attributeName, String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean getAttribute(String attributeName, boolean defaultValue) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Map<String, Object> getAttributes() throws CoreException {
		if (atList == null) {
			atList = new HashMap<String, Object>();
		}
		return atList;
	}

	@Override
	public Object[] getAttributes(String[] attributeNames) throws CoreException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getCreationTime() throws CoreException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getId() {
		return id;
	}

	@Override
	public IResource getResource() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getType() throws CoreException {
		return type;
	}

	@Override
	public boolean isSubtypeOf(String superType) throws CoreException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setAttribute(String attributeName, int value)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String attributeName, Object value)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	@Override
	public void setAttribute(String attributeName, boolean value)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	/* this is an Eclipse API so we cannot change it */
	@SuppressWarnings("unchecked")
	@Override
	public void setAttributes(Map attributes) throws CoreException {
		atList = attributes;

	}

	@Override
	public void setAttributes(String[] attributeNames, Object[] values)
			throws CoreException {
		// TODO Auto-generated method stub

	}

	/* This is an Eclipse api so we cannot change it. */
	@SuppressWarnings("unchecked")
	@Override
	public Object getAdapter(Class adapter) {
		// TODO Auto-generated method stub
		return null;
	}

}
