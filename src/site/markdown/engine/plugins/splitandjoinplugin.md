#Split & Joins 
The Imixs Split & Join plugin provides the functionality to create and update sub-process instances from an existing process instance (origin process),  or to update the origin process instance based on the processing instruction of a subprocess instance. 

	org.imixs.workflow.plugins.jee.SplitAndJoinPlugin

A 'split' means that a new subprocess instance will be created and linked to the current process instance (origin process). A 'join' means that a subprocess instance will update the origin process instance. The Split & Join definition is defined in the workflow result of a Imixs-BPMN Event definition using the [Imixs-BPMN modelling tool](../../modelling/index.html).   

The plugin evaluates the following items definition of a workflow result ("txtactivityResult"):
 
 * <strong>subprocess_create</strong> = create a new subprocess assigned to the current workitem 
 * <strong>subprocess_update</strong> = update an existing subprocess assigned to the current workitem
 * <strong>origin_update</strong> = update the origin process assigned to the current workitem
 
A subprocess instance will contain the ID of the origin process instance stored in the property $uniqueidRef. The origin process will contain a link to the subprocess stored in the property txtworkitemRef. So both workitems are linked together.
 
<img src="../../images/engine/split-and-join-ref.png"/> 
 
## Creating a new Subprocess
 
To create a new subprocess instance during the processing life cycle of an origin process, a item named 'subprocess_create' need to be defined in the corresponding activity result. See the following example: 
 
	<item name="subprocess_create">
		<modelversion>1.0.0</modelversion>
		<processid>100</processid>
		<activityid>10</activityid>
		<items>namTeam</items>
	</item>

This example will create a new subprocess instance with the model version '1.0.0' and the initial processID 100, which will be processed by the activityID 10. The tag 'items' defines a list of attributes to be copied from the origin process into the new subprocess.

Both workitems will be connected to each other. The subprocess will contain the $UniqueID of the origin process stored in the property $uniqueidRef. The origin process will contain a link to the subprocess stored in the property txtworkitemRef. So it is possible to navigate between the process instances.
 
It is also possible to define multiple subprocess definitions in one workflow result. For each separate definition a new subprocess will be created.

### Copy Attributes

The tag 'items' specifies a list of items to be copied from the origin workitem into the subprocess workitem. The tag may contain a list of items separated by comma. 

    <items>namTeam,txtName,_orderNumber</items>

To avoid item name conflicts the item name in the target workitem can be changed by separating the new item name by a '|' char. 

    <items>namTeam,txtName,_orderNumber|_origin_orderNumber</items>

In this example the item '_ordernumber' will be copied into the target workitem with the new item name '_origin_ordernumber'.

### Action result

After a new subprocess was created, an optional action result can be evaluated to overwrite the action result provided by the ResultPlugin.
The following example computes a new action result based on the uniqueId of the new subprocess:

	<item name="subprocess_create">
	    <modelversion>1.1.0</modelversion>
	    <processid>1000</processid>
	    <activityid>10</activityid>
	    <action>/pages/workitems/workitem.jsf?id=<itemValue>$uniqueid</itemValue></action>
	</item>



 
## Updating a Subprocess

To update an existing subprocess instance during the processing life cycle of the origin process, a item named 'subprocess_update' can be defined in the corresponding activity result. See the following example: 
 
	<item name="subprocess_update">
		<modelversion>1.0.0</modelversion>
		<processid>100</processid>
		<activityid>20</activityid>
		<items>namTeam</items>
	</item>


The activityid defines the workflow event to be processed on the matching subprocess instance. The tag 'items' defines the list of attributes to be added or updated from the origin process into the new subprocess.
Subprocesses and the origin process are connected to each other. The subprocess will contain the $UniqueID of the origin process stored in the property $uniqueidRef. The origin process will contain a link to the subprocess stored in the property txtworkitemRef.


### Regular Expressions
The definition accepts regular expressions to filter a subset of existing process instances. See the following example:

	<item name="subprocess_update">
		<modelversion>(^1.0)|(^2.0)</modelversion>
		<processid>(^1000$|^1010$)</processid>
		<activityid>20</activityid>
		<items>namTeam</items>
	</item>

This example applies to all existing subprocess instances with model versions starting with '1.0' or '2.0' and the processId 1000 or 1010.
To match all processIds between 1000 and 1999 the following regular expression can be applied:

	<item name="subprocess_update">
		<modelversion>(^1.0)|(^2.0)</modelversion>
		<processid>(1\d{3}))</processid>
		<activityid>20</activityid>
		<items>namTeam</items>
	</item>
 



## Updating the Origin Process

To join the data and status of a subprocess instance with the origin process instance a item named 'origin_update' can be defined in the activity result of a subprocess definition. 
Only one definition to update the origin process is allowed in a subprocess event. See the following example:

	<item name="origin_update">
		<activityid>20</activityid>
		<items>namTeam</items>
	</item>

The definition will update the origin process instance linked to the current subprocess. As the origin process instance is uniquely defined by the attribute $UniqueIDRef no further expression is needed in this case.   
The tag 'items' defines the list of attributes to be updated from the subprocess into the origin process.


### Action result

After the origin process was updated, an optional action result can be evaluated to overwrite the action result provided by the ResultPlugin.
The following example computes a new action result based on the uniqueId of the originprocess:

	<item name="subprocess_create">
	    <modelversion>1.1.0</modelversion>
	    <processid>1000</processid>
	    <activityid>10</activityid>
	    <action>/pages/workitems/workitem.jsf?id=<itemValue>$uniqueid</itemValue></action>
	</item>


