# openmrs-module-legacyui
- The legacy user interface for OpenMRS 1.9 is chiefly comprised of administrative functions and the patient dashboard. 
- Apparently, a new and more contemporary UI has been introduced via a UI framework and the legacy UI is kept around for 
administrative functions that are not yet implemented in the new UI.
- To retire the Legacy UI as planned, it is required to move the implementations and modules that still rely on it in order to 
maintain backwards compatibility.
- The main idea behind this project is to move legacy UI functions into an OpenMRS module that these implementations can install
until they are able to migrate away from it, since most of the implementations of OpenMRS around the world are running OpenMRS 1.9.

- It is compulsory to run openmrs-module-legacyui against legacyui branch of openmrs-core
that I have created to eventually remove legacyui.
