
node {
    try{
        params.REGIONS.split(',').each {
            def regionData = it.split("::")
            def regionCode = regionData[0].trim();
            def regionUrl = regionData[1].trim();
            def regionToken = regionData[2].trim();

            def

            echo regionCode
            echo regionUrl
            echo regionToken

            def url = regionUrl + "job/EXECUTE-JM-LOADTEST/lastBuild/stop"

            echo sh(script: "curl --location --request POST '$url' -u grinder:$regionToken", returnStdout: true).trim()
        }


    } catch (error) {
        echo error
    }
}

