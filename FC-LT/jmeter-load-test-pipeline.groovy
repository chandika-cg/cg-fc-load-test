import groovy.json.*

def props = [
    duration: Eval.me(params.DURATION),
    threadList: params.THREADS.split(','),
    delayList: params.DELAY.split(','),
    rampup: Eval.me(params.RAMPUP),
    interval: Eval.me(params.INTERVAL),
    testcaseList: params.RUN_TESTCASES_PARALLAY ? [params.TESTCASE.replace(',', ';')] : params.TESTCASE.split(','),
    rndResCnt: Eval.me(params.RND_RES_CNT),
    noResponse: params.NO_RESPONSE,
    debugMode: params.DEBUG_MODE,
    sourceCache: params.SOURCE_CACHE,
    timeout: Eval.me(params.TIMEOUT),
    jmeter_home: params.JMETER_HOME,
    log_file: params.LOG_FILE ?: 'jmeter',
    jmx_file: params.JMX_FILE,
    cnvId: params.CNV_ID,
    PRPP : params.PRPP,
    resultsCountList: params.RESULT_COUNT.split(','),
    loadInjectors: (params.LOAD_INJECTORS ?: "").replaceAll(/\w{1,3} \:\: /, ""),
    stageCount: 0,
    pipelineId: (new Date()).format("yyyyMMddHHmmss") + (Math.abs(new Random().nextInt() % [100]) + 1).toString(),
    buildSummary: [],
    addTC2CID: params.TC2CID ?: false,
    loopCount: params.LOOP_COUNT ?: -1,
    env: params.ENVIRONMENT ?: "NULL",
    authType: params.AUTH_TYPE ?: "KEY-AUTH",
    publishResults: params.PUBLISH_RESULTS
]

if(props.cnvId==""){
    props.cnvId = "LT" + props.pipelineId;
}

def description = "";
description += "\n+------------------------------------------------+";
description += "\n+  CNV_ID                  = ${props.cnvId}";
description += "\n+  DURATION                = ${props.duration} min";
description += "\n+  INTERVAL                = ${props.interval} s";
description += "\n+  RAMPUP                  = ${props.rampup} s";
description += "\n+  RESULT SHOW PROBABILITY = 1/${props.rndResCnt}";
description += "\n+  NO RESPONSE             = ${props.noResponse}";
description += "\n+  DEBUG MODE              = ${props.debugMode}";
description += "\n+  PUBLISH RESULTS         = ${props.publishResults}";
description += "\n+------------------------------------------------+";
echo description;

def runProject(props, testcase, resultsCount, threadCount, delay){
    def timeOut = props.duration + 15;
    def _cnvId = props.cnvId + "-" + props.stageCount + (props.addTC2CID?"-["+testcase.replace(';', '')+"]":'');
    def stageName = "${testcase}-T${threadCount}-D${delay}-R${resultsCount}";
    def executionId = "${props.pipelineId}-${props.stageCount}";

    stage stageName
    node {
        try{
            timeout(time: timeOut, unit: 'MINUTES') {
                def description = "";
                description += "\n+------------------------------------------------+";
                description += "\n+  CNV_ID                  = ${_cnvId}";
                description += "\n+  TESTCASE                = ${testcase}";
                description += "\n+  THREAD COUNT            = ${threadCount}";
                description += "\n+  DELAY                   = ${delay} ms";
                description += "\n+  RESULT COUNT            = ${resultsCount}";
                description += "\n+------------------------------------------------+";
                echo description;


                sh "mkdir -p ${props.jmeter_home}/prj/jtl"
                sh "mkdir -p ${props.jmeter_home}/prj/csv"

                def cmd = "${props.jmeter_home}/bin/jmeter.sh -n"
                cmd += " -j ${props.jmeter_home}/prj/${props.log_file}.log";
                cmd += " -l ${props.jmeter_home}/prj/jtl/${executionId}.jtl";
                cmd += " -t ${props.jmeter_home}/prj/${jmx_file}";

                if (props.loadInjectors != "")
                    cmd += " -R${props.loadInjectors}";

                cmd += " -JRND_RES_CNT=${props.rndResCnt}";
                cmd += " -JCNV_ID=${_cnvId}";
                echo "${testcase}";
                echo " -JTESTCASE=\"${testcase};\"";
                cmd += " -JTESTCASE=\"${testcase};\"";
                cmd += " -JTHREADS=${threadCount}";
                cmd += " -JRAMPUP=${props.rampup}";
                cmd += " -JDELAY=${delay}";
                cmd += " -JDURATION=${props.duration*60}";
                cmd += " -JNO_RESPONSE=${props.noResponse}";
                cmd += " -JDEBUG_MODE=${props.debugMode}";
                cmd += " -JLOOP_COUNT=${props.loopCount}";
                cmd += " -JSTARTUP_DELAY=0";
                cmd += " -JRESULT_COUNT=${resultsCount}";
                cmd += " -JTIMEOUT=${props.timeout}";
                cmd += " -JSOURCE_CACHE=${props.sourceCache}";
                cmd += " -JPRPP=\"${props.PRPP}\"";
                cmd += " -JENV=\"${props.env}\"";
                cmd += " -JAUTH_TYPE=\"${props.authType}\"";
                cmd += " -JPUBLISH_RESULTS=\"${props.publishResults}\"";

                cmd += "  -Dmule.xml.expandExternalEntities=true -Dmule.xml.expandInternalEntities=true";

                def startTime = (new Date()).format("yyyy-MM-dd HH:mm:ss");
                sh cmd;
                def endTime = (new Date()).format("yyyy-MM-dd HH:mm:ss");

                sh "${props.jmeter_home}/bin/JMeterPluginsCMD.sh --generate-csv ${props.jmeter_home}/prj/csv/${executionId}.csv --input-jtl ${props.jmeter_home}/prj/jtl/${executionId}.jtl --plugin-type AggregateReport";

                sh "python3 ${props.jmeter_home}/prj/PROCESS-LT-RESULTS.py ${props.jmeter_home}/prj/csv/${executionId}.csv ${testcase} ${threadCount} ${delay} ${resultsCount} ${_cnvId} ${props.duration} ${props.noResponse} ${startTime} ${endTime}"

                def lines = readFile("${props.jmeter_home}/prj/csv/${executionId}.csv").split('\n');
                def keys = lines[0].split(',')

                def rows = lines[1..-2].collect { line ->
                    def i = 0, vals = line.split(',')
                    keys.inject([:]) { map, key -> map << ["$key": vals[i++]] }
                }
                def _summary = [:];
                _summary.put(stageName + " :: " + _cnvId, rows);
                props.buildSummary.add(_summary);
                currentBuild.description = JsonOutput.prettyPrint(JsonOutput.toJson(props.buildSummary));
            }
        } catch (error) {
            println(error);
        }
    }
}

props.testcaseList.each {
    def testcase = it;
    props.resultsCountList.each {
        def resultsCount = Eval.me(it);
        props.threadList.each {
            def threadCount = it;
            props.delayList.each {
                def delay = Eval.me(it);
                props.stageCount++;
                runProject(props, testcase, resultsCount, threadCount, delay)
                sleep props.interval
            }
        }
    }
}