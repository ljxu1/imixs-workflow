package org.imixs.workflow.plugins;

import java.util.List;
import java.util.Vector;
import java.util.logging.Logger;

import org.imixs.workflow.ItemCollection;
import org.imixs.workflow.engine.WorkflowMockEnvironment;
import org.imixs.workflow.engine.plugins.AccessPlugin;
import org.imixs.workflow.exceptions.ModelException;
import org.imixs.workflow.exceptions.PluginException;
import org.junit.Before;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Test the ACL plug-in concerning the ACL settings in a process entity.
 * 
 * If ACL settings are provided by the next process entity than these settings
 * should be set per default and the activity entity can provide additional
 * setting.
 * 
 * 
 * e.g.
 * 
 * ProcessEntity 'namaddwriteaccess' = 'jo'
 * 
 * ActivityEntity 'namaddwriteaccess' = 'anna'
 * 
 * 
 * then $writeAccess should be 'jo','anna'
 * 
 * 
 * 
 * These tests extend the JUnit test in TestAccessPlugin
 * 
 * 
 * @author rsoika
 * 
 */
public class TestAccessPluginProcessEntity extends WorkflowMockEnvironment {

	private final static Logger logger = Logger.getLogger(TestAccessPluginProcessEntity.class.getName());

	AccessPlugin accessPlugin = null;
	ItemCollection documentContext;
	ItemCollection documentActivity, documentProcess;

	@Before
	public void setup() throws PluginException, ModelException {
		this.setModelPath("/bpmn/acl-test.bpmn");

		super.setup();

		accessPlugin = new AccessPlugin();
		try {
			accessPlugin.init(workflowContext);
		} catch (PluginException e) {

			e.printStackTrace();
		}

		// prepare data
		documentContext = new ItemCollection();
		logger.info("[TestAccessPlugin] setup test data...");
		Vector<String> list = new Vector<String>();
		list.add("manfred");
		list.add("anna");
		documentContext.replaceItemValue("namTeam", list);

		documentContext.replaceItemValue("namCreator", "ronny");

	}

	/**
	 * Test if the ACL settings will not be changed if no ACL is set be process
	 * or activity
	 * 
	 * @throws ModelException
	 ***/
	@Test
	public void testACLNoUpdate() throws ModelException {
		Vector<String> list = new Vector<String>();
		list.add("Kevin");
		list.add("Julian");
		documentContext.replaceItemValue("$writeaccess", list);

		documentActivity = this.getModel().getEvent(100, 10);

		try {
			accessPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		@SuppressWarnings("unchecked")
		List<String> writeAccess = documentContext.getItemValue("$WriteAccess");

		Assert.assertEquals(2, writeAccess.size());
		Assert.assertTrue(writeAccess.contains("Kevin"));
		Assert.assertTrue(writeAccess.contains("Julian"));

	}

	/**
	 * Test if the ACL settings from the next processEntity are injected into
	 * the workitem
	 * 
	 * @throws ModelException
	 **/
	@Test
	public void testACLfromProcessEntity() throws ModelException {

		documentActivity = this.getModel().getEvent(300, 10);
		documentContext.replaceItemValue("$processid", 300);

		try {
			accessPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		@SuppressWarnings("unchecked")
		List<String> writeAccess = documentContext.getItemValue("$WriteAccess");

		Assert.assertEquals(2, writeAccess.size());
		Assert.assertTrue(writeAccess.contains("joe"));
		Assert.assertTrue(writeAccess.contains("sam"));

	}

	/**
	 * Test if the ACL settings from the activityEntity are injected into the
	 * workitem
	 * 
	 * @throws ModelException
	 **/
	@Test
	public void testACLfromActivityEntity() throws ModelException {

		documentActivity = this.getModel().getEvent(100, 20);

		try {
			accessPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		@SuppressWarnings("unchecked")
		List<String> writeAccess = documentContext.getItemValue("$WriteAccess");

		Assert.assertEquals(3, writeAccess.size());
		Assert.assertTrue(writeAccess.contains("joe"));
		Assert.assertTrue(writeAccess.contains("samy"));
		Assert.assertTrue(writeAccess.contains("anna"));

	}

	/**
	 * Test if the ACL settings from the next processEntity are ignored in case
	 * the ActivityEnttiy provides settings. Merge is not supported!
	 * 
	 * @throws ModelException
	 **/
	@SuppressWarnings("unchecked")
	@Test
	public void testACLfromProcessEntityAndActivityEntity() throws ModelException {

		// set some old values
		Vector<String> list = new Vector<String>();
		list.add("Kevin");
		list.add("Julian");
		documentContext.replaceItemValue("namOwner", list);
		documentContext.replaceItemValue("$processid", 300);

		documentActivity = this.getModel().getEvent(300, 20);
		try {
			accessPlugin.run(documentContext, documentActivity);
		} catch (PluginException e) {

			e.printStackTrace();
			Assert.fail();
		}

		// $writeAccess= anna , manfred, joe, sam
		List<String> writeAccess = documentContext.getItemValue("$WriteAccess");
		Assert.assertEquals(3, writeAccess.size());
		Assert.assertTrue(writeAccess.contains("joe"));
		// Assert.assertTrue(writeAccess.contains("sam"));
		Assert.assertTrue(writeAccess.contains("manfred"));
		Assert.assertTrue(writeAccess.contains("anna"));

		// $readAccess= anna , manfred, joe, sam
		writeAccess = documentContext.getItemValue("$readAccess");
		Assert.assertEquals(2, writeAccess.size());
		Assert.assertTrue(writeAccess.contains("tom"));
		Assert.assertTrue(writeAccess.contains("manfred"));

	}

}
