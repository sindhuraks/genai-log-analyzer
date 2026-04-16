"use client";
import { useEffect, useState, useCallback } from "react";
import styles from "./page.module.css";

const MODELS = ["anthropic", "ollama", "openai"];
 
const MODEL_LABELS = {
  anthropic: "Claude",
  ollama: "Ollama",
  openai: "GPT",
};
 
const MODEL_COLORS = {
  anthropic: { active: "#7ec8a4", bg: "rgba(126,200,164,0.15)", border: "rgba(126,200,164,0.5)"},
  ollama:    { active: "#7ec8a4", bg: "rgba(126,200,164,0.15)", border: "rgba(126,200,164,0.5)" },
  openai:    { active: "#7ec8a4", bg: "rgba(126,200,164,0.15)", border: "rgba(126,200,164,0.5)"},
};
 
const ModelIndicator = ({ model, available }) => {
  const colors = MODEL_COLORS[model];
  
  return (
    <span
      title={`${MODEL_LABELS[model]}: ${available ? "explanation available" : "no explanation yet"}`}
      style={{
        display: "inline-flex",
        alignItems: "center",
        gap: "4px",
        fontSize: "9px",
        fontFamily: "'JetBrains Mono', monospace",
        fontWeight: "600",
        letterSpacing: "0.05em",
        textTransform: "uppercase",
        padding: "2px 6px",
        borderRadius: "3px",
        border: `1px solid ${available ? colors.border : "rgba(255,255,255,0.1)"}`,
        background: available ? colors.bg : "transparent",
        color: available ? colors.active : "rgba(255,255,255,0.2)",
        transition: "all 0.2s ease",
        userSelect: "none",
        cursor: "default",
      }}
    >
      <span
        style={{
          width: "5px",
          height: "5px",
          borderRadius: "50%",
          background: available ? colors.active : "rgba(255,255,255,0.15)",
          flexShrink: 0,
        }}
      />
      {MODEL_LABELS[model]}
    </span>
  );
};


const AnomalyDisplay = ({ onSelect, selectedId, query }) => {

    const [anomalies, setAnomalies] = useState([]);

    const sortAnomalies = (data) => {
        return data.sort((a, b) => {
            return [...data].sort((a, b) => a.id - b.id);
        });
    };

    const [modelAvailability, setModelAvailability] = useState({});

    const fetchModelAvailabilityForAnomaly = useCallback(async (anomalyId) => {
        const results = await Promise.all(
        MODELS.map(async (model) => {
            try {
            const res = await fetch(
                `http://localhost:8080/api/anomalies/${anomalyId}/explanation?model=${model}`,
                { method: "GET", headers: { "Content-Type": "application/json" } }
            );
            return { model, available: res.ok };
            } catch {
            return { model, available: false };
            }
        })
        );
        return results.reduce((acc, { model, available }) => {
        acc[model] = available;
        return acc;
        }, {});
    }, []);

    const fetchAllModelAvailability = useCallback(async (list) => {
        const BATCH = 5;
        for (let i = 0; i < list.length; i += BATCH) {
        const batch = list.slice(i, i + BATCH);
        const entries = await Promise.all(
            batch.map(async (anomaly) => {
            const availability = await fetchModelAvailabilityForAnomaly(anomaly.anomalyId);
            return [anomaly.anomalyId, availability];
            })
        );
        setModelAvailability((prev) => ({
            ...prev,
            ...Object.fromEntries(entries),
        }));
        }
    }, [fetchModelAvailabilityForAnomaly]);

    const fetchAnomalies = useCallback(async () => {
        try {
        const response = await fetch("http://localhost:8080/api/anomalies/list", {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });
    
        if (response.ok) {
            const data = await response.json();
            let list = [];
            if (Array.isArray(data)) {
            list = sortAnomalies(data);
            } else if (data && typeof data === "object") {
            list = sortAnomalies(Object.values(data).flat());
            }
            setAnomalies(list);
            fetchAllModelAvailability(list); // safe — defined above
        } else {
            setAnomalies([]);
        }
        } catch (error) {
        console.error("Error fetching anomalies:", error);
        setAnomalies([]);
        }
  }, [fetchAllModelAvailability]);

    // const fetchAnomalies = async () => {
    //     try {

    //         const response = await fetch("http://localhost:8080/api/anomalies/list", {
    //             method: "GET",
    //             headers: {
    //                 "Content-Type": "application/json",
    //             },
    //         });

    //         if (response.ok) {
    //             const data = await response.json();
    //             // console.log("API DATA:", data);

    //             if (Array.isArray(data)) {
    //                 setAnomalies(sortAnomalies(data));
    //             } else if (data && typeof data === "object") {
    //                 const flattened = Object.values(data).flat();
    //                 setAnomalies(sortAnomalies(flattened));
    //             } else {
    //                 setAnomalies([]);
    //             }
    //         } else {
    //             setAnomalies([]);
    //         }

    //     } catch (error) {
    //         console.error("Error fetching anomalies:", error);
    //         setAnomalies([]);
    //     }
    // };

    useEffect(() => {
        fetchAnomalies();
    }, []);

    const filteredAnomalies = anomalies.filter((anomaly) =>
        anomaly.anomalyId
        .toLowerCase()
        .includes(query.toLowerCase())
    );

//     return (
//         <div>
//            {/* {anomalies.length > 0 ? (
//                 anomalies.map((anomaly) => (
//                     <div key={anomaly.anomalyId} onClick={() => onSelect(anomaly.anomalyId)} style={{borderLeft: "2px solid #2bcbfb", paddingLeft: "10px", cursor:"pointer"}}>
//                         <div style={{
//                             display: "flex",
//                             justifyContent: "space-between",
//                             alignItems: "center",
//                             padding: "8px 10px 4px 0",
//                         }}>
//                             <p style={{color: "#4dd2fa", fontWeight:"bolder", fontSize: "15px"}}>{anomaly.anomalyId}</p>
//                             <p style={{color: anomaly.level?.toUpperCase() === "ERROR" ? "#E24B4A" : "#f0a500"}}> • {anomaly.level.toUpperCase()}</p>
//                         </div>
//                         <p style={{fontSize:"11px", fontFamily: "'JetBrains Mono', monospace",
//                         }}>{anomaly.content}</p>
//                         <div className={styles.searchDivider}></div>
//                     </div>
//                 ))
//             ) : (
//                 <p>No anomalies found</p>
//             )} */}

//             {filteredAnomalies.length > 0 ? (
//                 filteredAnomalies.map((anomaly) => (
//                 <div
//                     key={anomaly.anomalyId}
//                     onClick={() => onSelect(anomaly.anomalyId)}
//                     style={{
//                     borderLeft: "2px solid #2bcbfb",
//                     paddingLeft: "10px",
//                     cursor: "pointer",
//                     background:
//                         selectedId === anomaly.anomalyId
//                         ? "rgba(77,210,250,0.1)"
//                         : "transparent",
//                     }}
//                 >
//                     <div
//                     style={{
//                         display: "flex",
//                         justifyContent: "space-between",
//                     }}
//                     >
//                     <p style={{ color: "#4dd2fa", fontWeight: "bold" }}>
//                         {anomaly.anomalyId}
//                     </p>

//                     <p
//                         style={{
//                         color:
//                             anomaly.level?.toUpperCase() === "ERROR"
//                             ? "#E24B4A"
//                             : "#f0a500",
//                         }}
//                     >
//                         • {anomaly.level?.toUpperCase()}
//                     </p>
//                     </div>

//                     <p style={{ fontSize: "11px" }}>{anomaly.content}</p>
//                     <div className={styles.searchDivider}></div>
//                 </div>
//                 ))
//             ) : (
//                 <p>No matching anomalies</p>
//             )}
//         </div>
//     );
// }


return (
    <div>
      {filteredAnomalies.length > 0 ? (
        filteredAnomalies.map((anomaly) => {
          const availability = modelAvailability[anomaly.anomalyId];
          return (
            <div
              key={anomaly.anomalyId}
              onClick={() => onSelect(anomaly.anomalyId)}
              style={{
                borderLeft: "2px solid #2bcbfb",
                paddingLeft: "10px",
                cursor: "pointer",
                background:
                  selectedId === anomaly.anomalyId
                    ? "rgba(77,210,250,0.1)"
                    : "transparent",
              }}
            >
              <div
                style={{
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <p style={{ color: "#4dd2fa", fontWeight: "bold" }}>
                  {anomaly.anomalyId}
                </p>
                <p
                  style={{
                    color:
                      anomaly.level?.toUpperCase() === "ERROR"
                        ? "#E24B4A"
                        : "#f0a500",
                  }}
                >
                  • {anomaly.level?.toUpperCase()}
                </p>
              </div>
 
              <p style={{ fontSize: "11px" }}>{anomaly.anomalyId?.toLowerCase().startsWith("zoo")
    ? `[${[anomaly.node, anomaly.component].filter(Boolean).join(":")}] - ${anomaly.content}`
    : anomaly.content}</p>
 
              {/* Model availability indicators */}
              <div
                style={{
                  display: "flex",
                  gap: "4px",
                  flexWrap: "wrap",
                  marginBottom: "8px",
                  marginTop: "4px",
                }}
              >
                {availability ? (
                  MODELS.map((model) => (
                    <ModelIndicator
                      key={model}
                      model={model}
                      available={availability[model]}
                    />
                  ))
                ) : (
                  // Skeleton placeholders while loading
                  MODELS.map((model) => (
                    <span
                      key={model}
                      style={{
                        display: "inline-block",
                        width: "52px",
                        height: "18px",
                        borderRadius: "3px",
                        background: "rgba(255,255,255,0.05)",
                        animation: "pulse 1.4s ease-in-out infinite",
                      }}
                    />
                  ))
                )}
              </div>
 
              <div className={styles.searchDivider}></div>
            </div>
          );
        })
      ) : (
        <p>No matching anomalies</p>
      )}
    </div>
  );
};

export default AnomalyDisplay