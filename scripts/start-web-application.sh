#!/usr/bin/env bash

scriptDir="$(cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
$scriptDir/trucking-web-application-backend-0.4.0-SNAPSHOT/bin/trucking-web-application-backend -Dplay.crypto.secret="truckingiotonhdf" -Dplay.server.http.port=15100
