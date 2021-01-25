
node {
    try{
        params.REGIONS.split(',').each {
            def regionData = it.split("::")
            def regionCode = regionData[0].trim();
            def regionUrl = regionData[1].trim();
            def regionToken = regionData[2].trim();

            def url = regionUrl + "job/GET-JM-LT-TESTCASES/buildWithParameters?token=multiregion_test"


            echo sh(script: "curl $url -u grinder:$regionToken", returnStdout: true).trim()

        }

    } catch (error) {
        echo error
    }
}

