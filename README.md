This repo is part of the TrinReport project ([app](https://github.com/yosephbasileal/trin-report-app), [rddp](https://github.com/yosephbasileal/trin-report-rddp)).

### Dependencies
- Android Volley
- Tor-Onion-Proxy-Library

# TrinReport
TrinReport is an incident reporting system built for use by members of the Trinity College (Hartford) community, but can easily be adpated to work on other college campuses as well.

The project has two components. One is an android app for reporting incidents and a companion website RDDP, which stands for Reported Data Display Platform, that can be used by safety authorities to receive and handle reports.

The app comes with the following features:
- *Emergency Button*: sends user's identity and current location to RDDP, updats GPS location every 5 seconds and notifies the user when help is on the way.

- *Incident Report Form*: lets a user fill our an incident report, specify its urgency and attach relevant images. A user has the option to stay anonymous (in which case traffic is routed through Tor to hide the user's IP address from campus authorities).

- *Followup Chat*: the user will receive any follow up questions sent by a campus safety officer handling an incident report. This option is avaialble even if the original report was submitted anonymously through Tor.

RDDP comes with features that mirror the above:
- *Dashboard*: displays incoming emergency requests and incident reports. Shows near real-time location of person who made an emergency request. 

- *Emergency Dialog Box*: displays details of an emergency request. The page has a button for notifying the user when help is on the way.

- *Incident Report Panel*: displays details of an incident report and any attacehd images. Includes a chat window that can be used for following up on the report.
