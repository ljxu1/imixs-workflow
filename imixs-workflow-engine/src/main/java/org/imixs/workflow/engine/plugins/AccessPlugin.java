/*******************************************************************************
 *  Imixs Workflow 
 *  Copyright (C) 2001, 2011 Imixs Software Solutions GmbH,  
 *  http://www.imixs.com
 *  
 *  This program is free software; you can redistribute it and/or 
 *  modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation; either version 2 
 *  of the License, or (at your option) any later version.
 *  
 *  This program is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU 
 *  General Public License for more details.
 *  
 *  You can receive a copy of the GNU General Public
 *  License at http://www.gnu.org/licenses/gpl.html
 *  
 *  Project: 
 *  	http://www.imixs.org
 *  	http://java.net/projects/imixs-workflow
 *  
 *  Contributors:  
 *  	Imixs Software Solutions GmbH - initial API and implementation
 *  	Ralph Soika - Software Developer
 *******************************************************************************/

package org.imixs.workflow.engine.plugins;

import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;

/**
 * This plug-in implements a generic access management control (ACL) by
 * evaluating the configuration of a Activity Entity. The plug-in updates the
 * WorkItem attributes $ReadAccess and $WriteAccess depending on the provided
 * information.
 * 
 * <p>
 * These attributes defined in Activity Entity are evaluated by the plugin:
 * <ul>
 * <li>keyupdateacl (Boolean): if false no changes are necessary
 * <li>keyaddreadfields (Vector): Properties of the current WorkItem
 * <li>keyaddwritefields (Vector): Properties of the current WorkItem
 * <li>namaddreadaccess (Vector): Names & Groups to be added /replaced
 * <li>namaddwriteaccess (Vector): Names & Groups to be added/replaced
 * 
 * 
 * 
 * #Issue 90: Extend access plugin to resolve ACL settings in process entity
 * 
 * The AccessPlugin also evaluates the ACL settings in the next ProcessEntity
 * which is supported by newer versions of the imixs-bpmn modeler.
 * 
 * 
 * 
 * 
 * 
 * 
 * Fallback Mode:
 * 
 * NOTE: Models generated with the first version of the Imixs-Workflow Modeler
 * provide a different set of attributes. Therefore the plugin implements a
 * fallback method to support deprecated models. The fallback method evaluate
 * the following list of attributes defined in Activity Entity:
 * <p>
 * These attributes are:
 * <ul>
 * <li>keyaccessmode (Vector): '1'=update '0'=renew
 * <li>namaddreadaccess (Vector): Names & Groups to be added /replaced
 * <li>namaddwriteaccess (Vector): Names & Groups to be added/replaced
 * <li>keyaddreadfields (Vector): Attributes of the processd workitem to add
 * there values
 * <li>keyaddwritefields (Vector): Attributes of the processd workitem to add
 * therevalues
 * 
 * 
 * 
 * @author Ralph Soika
 * @version 3.0
 * @see org.imixs.workflow.WorkflowManager
 */

public class AccessPlugin extends AbstractPlugin {
	ItemCollection documentContext;
	ItemCollection documentActivity, documentNextProcessEntity;
	
	private static Logger logger = Logger.getLogger(AccessPlugin.class.getName());

	/**
	 * This method updates the $readAccess and $writeAccess attributes of a
	 * WorkItem depending to the configuration of a Activity Entity.
	 * 
	 * The method evaluates the new model flag keyupdateacl. If 'false' then acl
	 * will not be updated.
	 * 
	 * 
	 */
	@SuppressWarnings({ "rawtypes" })
	public ItemCollection run(ItemCollection adocumentContext, ItemCollection adocumentActivity) throws PluginException {
		documentContext = adocumentContext;
		documentActivity = adocumentActivity;

		// get next process entity
		int iNextProcessID = adocumentActivity.getItemValueInteger("numNextProcessID");
		String aModelVersion = adocumentActivity.getItemValueString("$modelVersion");
		try {
			documentNextProcessEntity = getCtx().getModelManager().getModel(aModelVersion).getTask(iNextProcessID);
		} catch (ModelException e) {
			// no next task defined (follow up)
			return documentContext;
		}
		// in case the activity is connected to a followup activity the
		// nextProcess can be null!

		// test update mode of activity and process entity - if true clear the
		// existing values.
		if (documentActivity.getItemValueBoolean("keyupdateacl") == false && (documentNextProcessEntity == null
				|| documentNextProcessEntity.getItemValueBoolean("keyupdateacl") == false)) {
			// no update!
			return documentContext;
		} else {
			// clear existing settings!
			documentContext.replaceItemValue("$readAccess", new Vector());
			documentContext.replaceItemValue("$writeAccess", new Vector());

			// activity settings will not be merged with process entity settings!
			if (documentActivity.getItemValueBoolean("keyupdateacl") == true) {
				updateACLByItemCollection(documentActivity);
			} else {
				updateACLByItemCollection(documentNextProcessEntity);
			}
		}

		return documentContext;
	}

	/**
	 * This method updates the read/write access of a workitem depending on a
	 * given model entity The model entity should provide the following
	 * attributes:
	 * 
	 * keyupdateacl,
	 * namaddreadaccess,keyaddreadfields,keyaddwritefields,namaddwriteaccess
	 * 
	 * 
	 * The method did not clear the exiting values of $writeAccess and
	 * $readAccess
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void updateACLByItemCollection(ItemCollection modelEntity) {

		if (modelEntity == null || modelEntity.getItemValueBoolean("keyupdateacl") == false) {
			// no update necessary
			return;
		}

		List vectorAccess;
		vectorAccess = documentContext.getItemValue("$readAccess");
		// add names
		mergeValueList(vectorAccess, modelEntity.getItemValue("namaddreadaccess"));
		// add Mapped Fields
		mergeFieldList(documentContext, vectorAccess, modelEntity.getItemValue("keyaddreadfields"));
		// clean Vector
		vectorAccess = uniqueList(vectorAccess);

		// update accesslist....
		documentContext.replaceItemValue("$readAccess", vectorAccess);
		if ((logger.isLoggable(Level.FINE)) && (vectorAccess.size() > 0)) {
			logger.fine("[AccessPlugin] ReadAccess:");
			for (int j = 0; j < vectorAccess.size(); j++)
				logger.fine("               '" + (String) vectorAccess.get(j) + "'");
		}

		// update WriteAccess
		vectorAccess = documentContext.getItemValue("$writeAccess");
		// add Names
		mergeValueList(vectorAccess, modelEntity.getItemValue("namaddwriteaccess"));
		// add Mapped Fields
		mergeFieldList(documentContext, vectorAccess, modelEntity.getItemValue("keyaddwritefields"));
		// clean Vector
		vectorAccess = uniqueList(vectorAccess);

		// update accesslist....
		documentContext.replaceItemValue("$writeAccess", vectorAccess);
		if ((logger.isLoggable(Level.FINE)) && (vectorAccess.size() > 0)) {
			logger.fine("[AccessPlugin] WriteAccess:");
			for (int j = 0; j < vectorAccess.size(); j++)
				logger.fine("               '" + (String) vectorAccess.get(j) + "'");
		}

	}

	
	
}
