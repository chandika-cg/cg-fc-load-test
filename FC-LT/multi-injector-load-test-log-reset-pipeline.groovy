
node {
    try{
        params.REGIONS.split(',').each {
            def regionData = it.split("::")
            def regionCode = regionData[0].trim();
            def regionUrl = regionData[1].trim();
            def regionToken = regionData[2].trim();



            def url = regionUrl + "job/JM-LOADTEST-LOGS/lastBuild/stop"

            echo sh(script: "curl --location --request POST '$url' -u grinder:$regionToken", returnStdout: true).trim()
        }

//        sleep 60 //wait 1Min
//
//        params.REGIONS.split(',').each {
//            def regionData = it.split("::")
//            def regionCode = regionData[0].trim();
//            def regionUrl = regionData[1].trim();
//            def regionToken = regionData[2].trim();
//
//            def url = regionUrl + "job/JM-LOADTEST-LOGS/buildWithParameters?token=multiregion_test"
//
//
//            echo sh(script: "curl $url -u grinder:$regionToken --data CLEAN_LOGS=YES", returnStdout: true).trim()
//
//        }

    } catch (error) {
        echo error
    }
}

