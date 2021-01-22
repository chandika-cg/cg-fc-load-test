


node {
    try{
        params.REGIONS.split(',').each {
            def regionData = it.split("::")
            def regionCode = regionData[0].trim();
            def regionUrl = regionData[1].trim();
            def regionToken = regionData[2].trim();

            echo regionCode
            echo regionUrl
            echo regionToken

            def url = regionUrl + "job/EXECUTE-JM-LOADTEST/lastBuild/kill"

            def dataCurl = ""

            echo sh(script: "curl $url -u grinder:$regionToken $dataCurl", returnStdout: true).trim()

        }


    } catch (error) {
        echo error
    }
}

