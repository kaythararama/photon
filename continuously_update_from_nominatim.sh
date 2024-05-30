#!/bin/bash -e

# For Nominatim < 3.7 set this to the Nominatim build directory.
# For newer versions, this must be the project directory of your import.
: ${NOMINATIM_DIR:=.}

while true
do
    starttime=`date +%s`

    # First consume updates in the Nominatim database.
    # The important part here is to leave out the indexing step. This
    # will be handled by Photon.

    nominatim replication --project-dir $NOMINATIM_DIR  --once

    # Now tell Photon to finish the updates and copy the new data into its
    # own database.
    curl http://localhost:2322/nominatim-update

    # Sleep a bit if updates take less than a minute.
    # If you consume hourly or daily diffs adapt the period accordingly.
    endtime=`date +%s`
    elapsed=$((endtime - starttime))
    if [[ $elapsed -lt 60 ]]
    then
        sleepy=$((60 - $elapsed))
        echo "Sleeping for ${sleepy}s..."
        sleep $sleepy
    fi

    # Now check if the updates have finished
    while [ `curl -s http://localhost:2322/nominatim-update/status` != '"OK"' ];
    do
        echo "Sleeping 15 more seconds."
        sleep 15
    done
done
