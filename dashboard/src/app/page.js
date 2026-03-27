"use client";
import { useState } from "react";
import styles from "./page.module.css";
import AnomalyDisplay from "./anomalydisplay";

const MODELS = ["claude 3 haiku", "gpt 4o mini", "llama3 chatqa"];
const TABS = ["Explanation", "Eval Metrics"];
const MODEL_KEY_MAP = {
  "claude 3 haiku": "anthropic",
  "gpt 4o mini": "openai",
  "llama3 chatqa": "ollama",
};

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

  const handleAnomalySelect =  async(anomalyId) => {
    setSelectedAnomaly(anomalyId);
    setLoadingLog(true);
    setLogError(null);
    setAnomalyLog(null);

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
                // value={query}
                // onChange={(e) => setQuery(e.target.value)}
              />
            </div>
            <div className={styles.searchDivider}></div>
            <div className={styles.anomalyDisp}>
              <AnomalyDisplay onSelect={handleAnomalySelect} selectedId={selectedAnomaly} />
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
                <button className={styles.evalBtn}> 
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
                    onClick={() => setActiveTab(tab)}
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
                        {anomalyLog.date && anomalyLog.time
                          ? `${anomalyLog.date} ${anomalyLog.time}`
                          : anomalyLog.date || anomalyLog.time}
                      ]{" "}
                      [{anomalyLog.level?.toLowerCase()}]{" "}
                      {anomalyLog.content || anomalyLog.message}
                    </p>
                  </div>
                </div>
              )}

              {!loadingLog && !logError && !anomalyLog && (
                <div className={styles.logPlaceholder}>
                  <p className={styles.logPlaceholderTxt}>
                    Select an anomaly from the sidebar to view log content.
                  </p>
                </div>
              )}
            </div>

            {!explanation && !loadingExplanation && !explanationError && (
              <div className={styles.analyzePrompt}>
                  <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="rgba(77,210,250,0.25)" strokeWidth="1.5"><polygon points="5 3 19 12 5 21 5 3"/></svg>
                  <p className={styles.analyzePromptTxt}> Select an error or warn message and click <strong>Analyze </strong> to view the explanation here.</p>
              </div>
            )}

            {loadingExplanation && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logPlaceholderTxt}>Analyzing anomaly...</p>
            </div>
          )}

          {explanationError && (
            <div className={styles.analyzePrompt}>
              <p className={styles.logErrorTxt}>⚠ {explanationError}</p>
            </div>
          )}

            {explanation && (
              <div className={styles.explanationBox}>
                <p className={styles.explanationText}>
                  {typeof explanation === "string"
                    ? explanation
                    : JSON.stringify(explanation, null, 2)}
                </p>
              </div>
            )}
          </div>
        </div>
      </main>
    </div>
  );
}
