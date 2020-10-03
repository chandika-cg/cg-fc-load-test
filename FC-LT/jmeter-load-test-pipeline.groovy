def props = [
    duration: Integer.parseInt(params.DURATION),
    threadList: params.THREADS.split(','),
    delayList: params.RAMPUP.split(','),
    interval: Integer.parseInt(params.INTERVAL),
    testcaseList: params.TESTCASE.split(','),
    rndResCnt: Integer.parseInt(params.RND_RES_CNT),
    noResponse: params.NO_RESPONSE,
    jmeter_home: params.JMETER_HOME,
    jmx_file: params.JMX_FILE,
    cnvId: params.CNV_ID,
    resultsCountList: params.RESULT_COUNT.split(','),
    stageCount: 0,
    pipelineId: (new Date()).format("yyyyMMddHHmmss") + (Math.abs(new Random().nextInt() % [100]) + 1).toString();
]


if(props.cnvId==""){
    props.cnvId = "LT" + props.pipelineId;
}

def description = "";
description += "\n+------------------------------------------------+";
description += "\n+  CNV_ID                  = ${props.cnvId}";
description += "\n+  DURATION                = ${props.duration} MIN";
description += "\n+  INTERVAL                = ${props.interval} S";
description += "\n+  RESULT SHOW PROBABILITY = 1/${props.rndResCnt}";
description += "\n+  NO RESPONSE             = ${props.noResponse}";
description += "\n+------------------------------------------------+";
echo description;

def runProject(props, testcase, resultsCount, threadCount, delay){
    def timeOut = props.duration + 5;
    duration = (props.duration*60).toString();
    def _cnvId = props.cnvId + "-" + props.stageCount;

    def executionId = props.pipelineId + props.stageCount.toString();
    stage "${props.testcase}-T${props.threadCount}-D${props.delay}-R${props.resultsCount}"
    node {
        try{
            timeout(time: timeOut, unit: 'MINUTES') {
                def description = "";
                description += "\n+------------------------------------------------+";
                description += "\n+  CNV_ID                  = ${_cnvId}";
                description += "\n+  TESTCASE                = ${testcase}";
                description += "\n+  THREAD COUNT            = ${threadCount}";
                description += "\n+  DELAY                   = ${delay} S";
                description += "\n+  RESULT COUNT            = ${resultsCount}";
                description += "\n+------------------------------------------------+";
                echo description;


                sh "mkdir -p reports"
                sh "mkdir -p summary"
                sh "mkdir -p reports/${executionId}"
                def cmd = "${props.jmeter_home}/bin/jmeter.sh -n"
                cmd += "-j ${props.jmeter_home}/prj/jmeter.log";
                cmd += "-l ${props.jmeter_home}/prj/summary/summary-report-${executionId}.jtl";
                cmd += "-t ${props.jmeter_home}/prj/${jmx_file}";


                cmd += " -JRND_RES_CNT=${props.rndResCnt}";
                cmd += "-JCNV_ID=${_cnvId}";
                cmd += "-JTESTCASE=${testcase}";
                cmd += "-JTHREADS=${threadCount}";
                cmd += "-JRAMPUP=${delay}";
                cmd += "-JDURATION=${props.duration}";
                cmd += "-JNO_RESPONSE=${props.noResponse}";
                cmd += "-JLOOP_COUNT=1";
                cmd += "-JSTARTUP_DELAY=0";
                cmd += "-JRESULT_COUNT=${resultsCount}";

                sh cmd;
            }
        } catch (error) {
            println(error);
        }
    }
}

props.testcaseList.each {
    def testcase = it;
    props.resultsCountList.each {
        def resultsCount = it;
        props.threadList.each {
            def threadCount = it;
            props.delayList.each {
                def delay = it;
                props.stageCount++;
                runProject(props, testcase, resultsCount, threadCount, delay)
            }
        }
        sleep props.interval
    }
}