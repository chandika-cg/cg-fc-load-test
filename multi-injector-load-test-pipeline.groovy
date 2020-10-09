
def duration = Integer.parseInt(params.DURATION)
def threadList = params.THREAD_LIST.split(',')
def regionList = params.REGIONS.split(',')
def delayList = params.DELAY_LIST.split(',')
def interval = Integer.parseInt(params.INTERVAL) * 60
def email_prefix = params.EMAIL_PREFIX
def testProjectFilename = params.TEST_PROJECT_FILENAME
def soapuiConfigs = params.SOAPUI_CONFIGS
def profiles = params.PROFILES.split(',')
// def cnvId = (new Date()).format("yyyyMMddHHmmss")


node {
    try{
        def branches = [:]
        params.REGIONS.split(',').each {
            def regionData = it.split("::")
            def regionCode = regionData[0].trim();
            def regionUrl = regionData[1].trim();
            def regionToken = regionData[2].trim();

            echo regionCode
            echo regionUrl
            echo regionToken

            def url = regionUrl + "job/EXECUTE-JM-LOADTEST/buildWithParameters?token=multiregion_test"

            def cnvId = params.CNV_ID;
            if(cnvId==""){
                cnvId = "LT" + (new Date()).format("yyyyMMddHHmmss") + (Math.abs(new Random().nextInt() % [100]) + 1).toString();
            }

            def params = [
                    'DURATION' :params.DURATION,
                    'TESTCASE' : params.TESTCASE,
                    'RAMPUP' : params.RAMPUP,
                    'DELAY' : params.DELAY,
                    'THREADS' : params.THREADS,
                    'CNV_ID' : cnvId + "-" + regionCode,
                    'INTERVAL' : params.INTERVAL,
                    'RND_RES_CNT' : params.RND_RES_CNT,
                    'JMETER_HOME' : params.JMETER_HOME,
                    'JMX_FILE' : params.JMX_FILE,
                    'RESULT_COUNT' : params.RESULT_COUNT,
                    'TIMEOUT' : params.TIMEOUT
            ]

            def dataCurl = ""
            for( entry in params )
            {
                url += " --data " + entry.key + "=" + entry.value + " " //java.net.URLEncoder.encode(entry.value, "UTF-8");
            }


            branches[regionCode] = {
                node{
                    echo sh(script: "curl $url -u grinder:$regionToken $dataCurl", returnStdout: true).trim()
                }
            }
            parallel branches
        }
        sleep timeOut

    } catch (error) {
        echo error
    }
}

