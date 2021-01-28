import java.security.UnrecoverableKeyException

def cnvId = params.CNV_ID;
def refreshInterval = Eval.me(params.REFRESH_INTERVAL) * 60;
if (cnvId == "") {
    cnvId = "LT" + (new Date()).format("yyyyMMddHHmmss") + (Math.abs(new Random().nextInt() % [100]) + 1).toString();
}

echo "+-------------------------------------------------------------+";
echo "+    CNV_ID = " + cnvId;
echo "+-------------------------------------------------------------+";

def activeRegions = params.REGIONS.split(',');
int spareRegionI = activeRegions[activeRegions.length-1];
def spareRegion = activeRegions[spareRegionI];
activeRegions.remove(spareRegionI);

def startLT(def regionInfo)
{
    def regionData = regionInfo.split("::");
    def regionCode = regionData[0].trim();
    def regionUrl = regionData[1].trim();
    def regionToken = regionData[2].trim();

    echo regionCode
    echo regionUrl
    echo regionToken

    def url = regionUrl + "job/EXECUTE-JM-LOADTEST/buildWithParameters?token=multiregion_test"

    def params = [
            'DURATION'              : params.DURATION,
            'TESTCASE'              : params.TESTCASE,
            'RAMPUP'                : refreshI == 0 ? params.RAMPUP : "0",
            'DELAY'                 : params.DELAY,
            'THREADS'               : params.THREADS,
            'CNV_ID'                : cnvId + "-" + regionCode,
            'INTERVAL'              : params.INTERVAL,
            'RND_RES_CNT'           : params.RND_RES_CNT,
            'JMETER_HOME'           : params.JMETER_HOME,
            'JMX_FILE'              : params.JMX_FILE,
            'RESULT_COUNT'          : params.RESULT_COUNT,
            'TIMEOUT'               : params.TIMEOUT,
            'PRPP'                  : params.PRPP,
            'RUN_TESTCASES_PARALLAY': params.RUN_TESTCASES_PARALLAY,
    ]

    def dataCurl = ""
    for (entry in params) {
        if (entry.key == 'TESTCASE') {
            for (val in entry.value.toString().split(",")) {
                url += " --data " + entry.key + "=" + val + " ";
            }
        } else {
            url += " --data " + entry.key + "=" + entry.value + " ";
            //java.net.URLEncoder.encode(entry.value, "UTF-8");
        }
    }

    echo sh(script: "curl $url -u grinder:$regionToken $dataCurl", returnStdout: true).trim()
}

def cancelLT(def regionInfo)
{
    def regionData = regionInfo.split("::");
    def regionCode = regionData[0].trim();
    def regionUrl = regionData[1].trim();
    def regionToken = regionData[2].trim();

    def url = regionUrl + "job/EXECUTE-JM-LOADTEST/lastBuild/stop";
    echo sh(script: "curl --location --request POST '$url' -u grinder:$regionToken", returnStdout: true).trim();

}

node {

    try {

        activeRegions.each {
            startLT(it)
        }

        while(refreshInterval>0)
        {
            sleep refreshInterval;

            def curRegions =activeRegions.clone();
            for(int i=0; i<curRegions.length; i++)
            {
                cancelLT(curRegions[i]);

                def newRegion = spareRegion;
                spareRegion = curRegions[i];

                echo "+-------------------------------------------------------------+";
                echo "+    RESTART";
                echo "+    NEW   : " + newRegion;
                echo "+    SPARE : " + spareRegion;
                echo "+-------------------------------------------------------------+";


                startLT(newRegion);

                sleep 60*4;

                activeRegions.remove(i);
                activeRegions << newRegion;
            }
        }

    } catch (error) {
        echo error
    }
}

