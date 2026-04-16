"use client";
import { useState , useMemo} from "react";
import styles from "./page.module.css";
import AnomalyDisplay from "./anomalydisplay";

const MODELS = ["claude 3 haiku", "gpt 4o mini", "llama3 chatqa"];
const TABS = ["Explanation", "Eval Metrics"];
const MODEL_KEY_MAP = {
  "claude 3 haiku": "anthropic",
  "gpt 4o mini": "openai",
  "llama3 chatqa": "ollama",
};

const SECTION_COLORS = ["#ff6b6b", "#a29bfe", "#00b894"];
function parseNumberedSections(text) {
    //  if (!text) return [];

    // // Split by numbered headings (1. Heading: ...)
    // // const sectionRegex = /^(\d+)\.\s+(.+?)\?\s*$/gm;
    // // const matches = [...text.matchAll(sectionRegex)];

    // return [{ heading: "Explanation", content: text, color: SECTION_COLORS[2] }];

    // // const sections = matches.map((match, i) => {
    // //   const start = match.index + match[0].length;
    // //   const end = matches[i + 1]?.index ?? text.length;
    // //   const content = text.slice(start, end).trim();
    // //   return {
    // //     heading: match[2].trim(),
    // //     content: content || match[3] || "", // include inline if any
    // //     color: SECTION_COLORS[i] ?? "#4dd2fa",
    // //   };
    // // });

    // // return sections;
     if (!text) return [];

  const sectionRegex = /(\d+)\.\s+([^:\n?]+[:?]?)\s*([\s\S]*?)(?=\n\d+\.|\n*$)/g;
  const matches = [...text.matchAll(sectionRegex)];

  return matches
    .map((match, i) => ({
      number: Number(match[1]),
      heading: match[2].trim(),
      content: match[3].trim(),
      color: SECTION_COLORS[i] ,
    }))
    .filter((section) => section.number >= 1 && section.number <= 3);
}

function formatDateTime(date, time) {
  try {
    // Apache: single "Time" field — full natural-language timestamp
    if (!date && time) {
      const parsed = new Date(time);
      if (!isNaN(parsed)) {
        const yyyy = parsed.getFullYear();
        const MM   = String(parsed.getMonth() + 1).padStart(2, "0");
        const dd   = String(parsed.getDate()).padStart(2, "0");
        const HH   = String(parsed.getHours()).padStart(2, "0");
        const mm   = String(parsed.getMinutes()).padStart(2, "0");
        const ss   = String(parsed.getSeconds()).padStart(2, "0");
        return `${yyyy}-${MM}-${dd} ${HH}:${mm}:${ss}`;
      }
      return time; // fallback: return as-is
    }
 
    // HDFS: date="081109" → "08-11-09" → 2008-11-09, time="203615" → "20:36:15"
    if (date && /^\d{6}$/.test(date.trim())) {
      const d = date.trim();
      const formattedDate = `20${d.slice(0, 2)}-${d.slice(2, 4)}-${d.slice(4, 6)}`;
      const t = (time || "").trim();
      const formattedTime = t.length === 6
        ? `${t.slice(0, 2)}:${t.slice(2, 4)}:${t.slice(4, 6)}`
        : t;
      return `${formattedDate} ${formattedTime}`;
    }
 
    // Zookeeper: date="2015-07-29", time="17:41:44,747" → strip milliseconds
    if (date && /^\d{4}-\d{2}-\d{2}$/.test(date.trim())) {
      const formattedTime = (time || "")
        .replace(/"/g, "")       // strip any surrounding quotes
        .replace(/,\d+$/, "")   // drop ",747" milliseconds
        .trim();
      return `${date.trim()} ${formattedTime}`;
    }
 
    return [date, time].filter(Boolean).join(" ");
  } catch {
    return [date, time].filter(Boolean).join(" ");
  }
}

export default function Home() {

  const [activeModel, setActiveModel] = useState("claude 3 haiku");
  const [activeTab, setActiveTab] = useState("Explanation");
  const [selectedAnomaly, setSelectedAnomaly] = useState(null);
  const [logError, setLogError] = useState(null);
  const [anomalyLog, setAnomalyLog] = useState(null);
  const [loadingLog, setLoadingLog] = useState(false);
  const [analyzed, setAnalyzed] = useState(false);
  const [explanation, setExplanation] = useState(null);
  const [loadingExplanation, setLoadingExplanation] = useState(false);
  const [explanationError, setExplanationError] = useState(null);
  const [isCached, setIsCached] = useState(false);
  const [evaluation, setEvaluation] = useState(null);
  const [loadingEvaluation, setLoadingEvaluation] = useState(false);
  const [evaluationError, setEvaluationError] = useState(null);
  const [query, setQuery] = useState("");

  const handleAnomalySelect =  async(anomalyId) => {
    setSelectedAnomaly(anomalyId);
    setExplanation(null);
    setExplanationError(null);
    setIsCached(false);
    setLoadingLog(true);
    setLogError(null);
    setAnomalyLog(null);
    setEvaluation(null);        
    setEvaluationError(null);

    try {
      const response = await fetch(`http://localhost:8080/api/anomalies/${anomalyId}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
      });
      if(response.ok) {
        const data = await response.json();
        console.log(data);
        setAnomalyLog(data);
      } else {
        throw new Error(`Error ${res.status}`);
      }
    } catch (err) {
      setLogError(err.message);
    } finally {
      setLoadingLog(false);
    }
  }

  const handleAnalyzeAnomaly = async() => {
    if (!selectedAnomaly) return;

    const model = MODEL_KEY_MAP[activeModel];
    setLoadingExplanation(true);
    setExplanationError(null);
    setExplanation(null);
    setIsCached(false);

    // check db
    try {
      const dbRes = await fetch(`http://localhost:8080/api/anomalies/${selectedAnomaly}/explanation?model=${model}`, {
            method: "GET",
            headers: {
                "Content-Type": "application/json",
            },
      });

      if (dbRes.ok) {
        const data = await dbRes.json();
        setExplanation(data);
        setIsCached(true);
        return;
      }

      const llmRes = await fetch(`http://localhost:8080/api/anomalies/${selectedAnomaly}/explain/${model}`, {
        method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
      })

      if(llmRes) {
        const data = await llmRes.json();
        const text =
        data.claude_explanation ||
        data.openai_explanation ||
        data.ollama_explanation ||
        "No explanation returned.";
        setExplanation(text);
      } else {
        throw new Error(`Error ${res.status}`);
      }

    } catch (err) {
      setExplanationError(err.message);
    } finally {
      setLoadingExplanation(false);
    }
  }
  // const formatExplanation = (text) => {
  //   if (!text) return "";

  //   // extract sections 1-5 (capture till uncertainty)
  //   const pattern = text.match(/([\s\S]*?Uncertainty:.*?)(\n|$)/);
  //   return pattern ? pattern[0] : text;
  // }
  
  const explanationText = useMemo(() => {
    if (!explanation) return "";
    return typeof explanation === "string" ? explanation : explanation.explanation || "";
  }, [explanation]);

  const confidence = useMemo(() => {
    const match = explanationText.match(/4\.\s+Confidence\s*:\s*([\d.]+)/i);
    return match ? parseFloat(match[1]) : null;
}, [explanationText]);

  const uncertainty = useMemo(() => {
    console.log('Uncertainty : ' + explanationText);
    const match = explanationText.match(/5\.\s+Uncertainty\s*:\s*([\D.]+)/i);
    console.log('Uncertainty : ' + match);
    return match ? match[1] : null;
}, [explanationText]);
 
  const sections = useMemo(
    () => parseNumberedSections(explanationText),
    [explanationText]
  );

  const handleEvaluateResponse = async() => {
    if (!explanationText) return;
    console.log('Explanation : ' + explanationText);

    const model = MODEL_KEY_MAP[activeModel];
    console.log('Model : ' + model);

    setLoadingEvaluation(true);
    setEvaluationError(null);
    setEvaluation(null);

    try {
      const llmRes = await fetch(`http://localhost:8080/api/llm/${selectedAnomaly}/${model}/eval`, {
        method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
      })

      if(llmRes) {
        const data = await llmRes.json();
        const text =
        data.claude_evaluation ||
        "No evaluationn returned.";
        setEvaluation(text);
      } else {
        throw new Error(`Error ${res.status}`);
      }

    } catch (err) {
      setEvaluationError(err.message);
    } finally {
      setLoadingEvaluation(false);
    }
  }


    // const formatExplanation = (text) => {
  //   if (!text) return "";

  //   // extract sections 1-5 (capture till uncertainty)
  //   const pattern = text.match(/([\s\S]*?Uncertainty:.*?)(\n|$)/);
  //   return pattern ? pattern[0] : text;
  // }


  const evalMetrics = (evaluation) => {
    if (!evaluation) return [];

    let data;

    if (typeof evaluation === "string") {
      try {
        const cleaned = evaluation
          .replace(/```json/g, "")
          .replace(/```/g, "")
          .trim();

        data = JSON.parse(cleaned);
      } catch (err) {
        console.error("Invalid evaluation JSON:", err);
        return [];
      }
    } else {
      data = evaluation;
    }

    return [
      { label: "Correctness", value: data.correctness },
      { label: "Completeness", value: data.completeness },
      { label: "Clarity", value: data.clarity },
      { label: "Answer Relevance", value: data.answer_relevance },
      { label: "Reason", value: data.reason },
    ];
  }
  // , [evaluation];
  
  return (
    <div className={styles.page}>
      <main className={styles.main}>
        <div className={styles.horizontalBar}>
            {/* <div> 🛡 </div> */}
            <h1 className={styles.logoText}>LogGuardianAI</h1>
        </div>

        <div className={styles.container}>
          <div className={styles.leftSection}>
            <h3> ANOMALIES</h3>
            <div className={styles.searchWrapper}>
              <svg className={styles.searchIcon} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
              <input
                className={styles.searchInput}
                type="text"
                placeholder="Search anomaly ID..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
              />
            </div>
            <div className={styles.searchDivider}></div>
            <div className={styles.anomalyDisp}>
              <AnomalyDisplay onSelect={handleAnomalySelect} selectedId={selectedAnomaly} query={query}/>
            </div>
          </div>
          <div className={styles.rightSection}>
             <div className={styles.anomalyTopbar}>
              <h5 className={styles.sectionTxt}>{selectedAnomaly ? "Selected anomaly : " + selectedAnomaly : "Select an anomaly"}</h5>
              {/* <p style={{color: anomalyLog?.level?.toUpperCase() === "ERROR" ? "#E24B4A" : "#f0a500"}}> • {anomalyLog.level.toUpperCase()}</p> */}
              <div className={styles.btnGrp}>
                <button className={styles.analyzeBtn} onClick={handleAnalyzeAnomaly}> 
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                  {loadingExplanation ? "Analyzing . . ." : "Analyze"}
                </button>
                <button className={styles.evalBtn} onClick={handleEvaluateResponse}> 
                  <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"><path d="M9 11l3 3L22 4"/><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/></svg>
                  Run Eval
                </button> 
              </div>
            </div>
            <div className={styles.anomalyMidbar}>
                <h5 className={styles.midsectionTxt}>MODEL</h5>
                <div className={styles.modelBtnGrp}>
                  {MODELS.map((model) => (
                    <button
                      key={model}
                      className={`${styles.modelBtn} ${activeModel === model ? styles.modelBtnActive : ""}`}
                      onClick={() => {
                      setActiveModel(model);
                      setExplanation(null);
                      setExplanationError(null);
                      setEvaluation(null);
                    }}
                    >
                      {model}
                    </button>
                ))}
                </div>
            </div>
            <div className={styles.anomalyMidbar2}>
                  {TABS.map((tab) => (
                    <h5 
                    key={tab}
                    className={`${styles.midsectionTxt2} ${
                      activeTab === tab ? styles.activeTab : ""
                    }`}
                    onClick={() => {
                      setActiveTab(tab);

                      // if (tab === "Eval Metrics") {
                      //   setExplanation(null);
                      //   setExplanationError(null);
                      // }
                    }}
                     > 
                  {tab}
                  </h5>
                  ))}
            </div>
            <div className={styles.anomalyLogDisp}>
              <h6 className={styles.logTxt}>ANOMALY LOG</h6>
              {/* <div className={styles.logPlaceholder}>
                <p className={styles.logPlaceholderTxt}>Select an anomaly from the sidebar to view log content.</p>
               </div> */}

              {loadingLog && (
                <div className={styles.logPlaceholder}>
                  <p className={styles.logPlaceholderTxt}>
                    <span className={styles.logLoading}>Fetching log</span>
                  </p>
                </div>
              )}

              {logError && (
                <div className={styles.logPlaceholder}>
                  <p className={styles.logErrorTxt}>⚠ {logError}</p>
                </div>
              )}

              {!loadingLog && !logError && anomalyLog && (
                <div className={styles.logPlaceholder}>
                  <div className={styles.logMeta}>
                    <p className={styles.logContent}>
                      [
                        {/* {anomalyLog.date && anomalyLog.time
                          ? `${anomalyLog.date} ${anomalyLog.time}`
                          : anomalyLog.date || anomalyLog.time} */}
                          {formatDateTime(anomalyLog.date, anomalyLog.time)}
                      ]{" "}
                      [{anomalyLog.level?.toLowerCase()}]{" "}
                      {/* {anomalyLog.content || anomalyLog.message} */}
                      {selectedAnomaly?.toLowerCase().startsWith("zoo")
                        ? `[${[anomalyLog.node, anomalyLog.component].filter(Boolean).join(":")}] - ${anomalyLog.content || anomalyLog.message}`
                        : anomalyLog.content || anomalyLog.message}
                    </p>
                  </div>
                </div>
              )}

              {!loadingLog && !evaluation && !logError && !anomalyLog && (
                <div className={styles.logPlaceholder}>
                  <p className={styles.logPlaceholderTxt}>
                    Select an anomaly from the sidebar to view log content.
                  </p>
                </div>
              )}
            </div>

            {!explanation && !evaluation && !loadingExplanation && !explanationError && (
              <div className={styles.analyzePrompt}>
                <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(77,210,250,0.25)" strokeWidth="1.5"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                {activeTab == 'Explanation' ? (
                  <p className={styles.analyzePromptTxt}> Select an error or warn message and click <strong>Analyze </strong> to view the explanation here.</p>
                ) : (
                   <p className={styles.analyzePromptTxt}>
                    Click <strong> Run Eval </strong> to view model performance metrics for this anomaly.
                  </p>
                )}
                  
              </div>
            )}

            {loadingExplanation && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logPlaceholderTxt}>Analyzing anomaly...</p>
            </div>
          )}

          {loadingEvaluation && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logPlaceholderTxt}>Running LLM-as-a-judge evaluation...</p>
            </div>
          )}

          {explanationError && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logErrorTxt}>⚠ {explanationError}</p>
            </div>
          )}

          {evaluationError && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logErrorTxt}>⚠ {explanationError}</p>
            </div>
          )}

            {/* {explanation && (
              <div className={styles.analyzePrompt}>
                <p className={styles.explanationText}>
                  {formatExplanation (
                  typeof explanation === "string"
                    ? explanation
                    : explanation.explanation)}
                </p>
              </div>
            )} */}

            {activeTab === "Eval Metrics" && evaluation && (
              <div className={styles.evaluationArea}>
                 <div className={styles.metricsGrid}>
                  {evalMetrics(evaluation)
                    .filter((m) => m.label !== "Reason")
                    .map((metric, i) => {
                      const score = Math.round(metric.value * 100);

                      return (
                        <div key={i} className={styles.metricCard}>
                          <span className={styles.metricLabel}>
                            {metric.label}
                          </span>
                          <div
                            className={styles.circularChart}
                            style={{ "--score": score }}
                          >
                            <span className={styles.metricScore}>{(metric.value * 10).toFixed(1)}</span>
                          </div>
                        </div>
                      );
                    })}
                </div>
                
                <div className={styles.reasonRow}>
                  <div className={styles.reasonTitle}>Reason</div>
                  <p className={styles.reasonText}>
                    {evalMetrics(evaluation).find(m => m.label === "Reason")?.value}
                  </p>
                </div>                    
              </div>
            )}

              {activeTab === "Explanation" && explanation && (
                <div className={styles.explanationArea}>
                  <div className={styles.topRow}>
                    <p className={styles.modelTxt}> {activeModel}</p>
                     <div className={styles.rightStats}>
                      {confidence !== null && (
                          <div className={styles.confWrap}>
                            <span className={styles.confLabel}>Confidence</span>
                            <div className={styles.confBarTrack}>
                              <div
                                className={styles.confBarFill}
                                style={{ width: `${Math.round(confidence * 100)}%` }}
                              />
                            </div>
                            <span className={styles.confPct}>{Math.round(confidence * 100)}%</span>
                          </div>
                        )}
                        {uncertainty && (
                          <div className={styles.confWrap}>
                            <span className={styles.confLabel}>Uncertainty</span>
                            <span className={styles.uncPill}>{uncertainty.toLowerCase()}</span>
                          </div>
                        )}
                      </div>  
                    </div>
                  {sections.length > 0 ? (
                    <div className={styles.sectionContainer}>
                      {sections.map((section, i) => (
                        <div
                          key={i}
                          className={styles.sectionBox}
                          style={{ "--section-color": section.color }}
                        >
                          <div className={styles.sectionHeader}>
                            <span className={styles.sectionTitle}>{section.heading}</span>
                          </div>
                          <p className={styles.sectionContent}>{section.content}</p>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className={styles.explanationText}>{explanationText}</p>
                  )}
                </div>
              )}
          </div>
        </div>
      </main>
    </div>
  );
}
