#The Workflow Service

The resource _/workflow_ provides methods to create, process and read workitems through the Imixs-Rest API.
 
 

## GET a Workitem

The subresource _/workflow/workitem/_ provides GET methods to read the content of a workitem:


| URI                                           | Method | Description                               | 
|-----------------------------------------------|--------|-----------------------------------|
| /workflow/workitem/{uniqueid}                 | GET    | a single workitem represented by the   provided uniqueid                              |
| /workflow/workitem/{uniqueid}/file/{file}     | GET    | a file attachment located in the property   $file of the spcified workitem           |


## GET a Task List 
The subresource _/workflow/tasklist/_ provides GET methods to read collections of workitems:

| URI                                           | Method | Description                               | 
|-----------------------------------------------|--------|-----------------------------------|
| /workflow/worklist                            | GET    | a collection of workitems representing the worklist for the current user |             
| /workflow/tasklist/owner/{owner}              | GET    | a collection of workitems owned by a specific  user (or value 'null' for the current user)   |
| /workflow/tasklist/creator/{creator}          | GET    | a collection of workitems created by a specific user (or value 'null' for the current user)                           |
| /workflow/tasklist/processid/{processid}      | GET    | a collection of workitems in a specific    process state             |
| /workflow/tasklist/group/{processgroup}       | GET    | a collection of workitems in a specific    process group                             |
| /workflow/tasklist/ref/{uniqueid}             | GET    | a collection of workitems referenced to a  specific uniqueId (childs)                |



## PUT/POST a Workitem or Task List
The methods PUT, POST allow to create and process a workitem or a task list:


| URI                          | Method  | Description                               | 
|------------------------------|---------|----------------------------------|
| /workflow/workitem           | POST    | posts a workitem, to be processed by the  workflow manager. To update an existing workitem, the attribute $uniqueid must be provided as part of the data structure. The media types application/xml, application/json and x-www-form-urlencoded are supported.   |
| /workflow/workitem/{uniqueid}| POST    | posts a workitem by uniqueid, to be processed by the  workflow manager. The media types application/xml, application/json and x-www-form-urlencoded are supported.   |
| /workflow/tasklist           | POST    | posts a list of workitems to be processed by the  workflow manager. The media type application/xml is supported.   |



## Resource Options
With the following optional URI parameters a request can be filtered: 


| option      | description                                         | example               |
|-------------|-----------------------------------------------------|-----------------------|
| pagesize    | number of documents returned                        | ..?pagesize=10           |
| pageindex   | page index to start                                 | ..?pageindex=5&pagesize=10   |
| type        | filter workitems by the 'type' property             | ..?type=workitem      | 
		

<strong>Note:</strong> The Imixs-Workflow manages the access to workitems by individual access lists per each entity. The result of a collection of workitems depends on the current user access-level and read access permissions for a workitem. Read also the section [Access Control](/engine/acl.html) for further information. 
  
   