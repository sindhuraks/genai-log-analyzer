"use client";
import { useEffect, useState } from "react";
import styles from "./page.module.css";

const AnomalyDisplay = () => {

    const [anomalies, setAnomalies] = useState([]);

    const sortAnomalies = (data) => {
        return data.sort((a, b) => {
            return [...data].sort((a, b) => a.id - b.id);
        });
    };

    const fetchAnomalies = async () => {
        try {

            const response = await fetch("http://localhost:8080/api/anomalies/list", {
                method: "GET",
                headers: {
                    "Content-Type": "application/json",
                },
            });

            if (response.ok) {
                const data = await response.json();
                // console.log("API DATA:", data);

                if (Array.isArray(data)) {
                    setAnomalies(sortAnomalies(data));
                } else if (data && typeof data === "object") {
                    const flattened = Object.values(data).flat();
                    setAnomalies(sortAnomalies(flattened));
                } else {
                    setAnomalies([]);
                }
            } else {
                setAnomalies([]);
            }

        } catch (error) {
            console.error("Error fetching anomalies:", error);
            setAnomalies([]);
        }
    };

    useEffect(() => {
        fetchAnomalies();
    }, []);

    return (
        <div>
           {anomalies.length > 0 ? (
                anomalies.map((anomaly) => (
                    <div key={anomaly.anomalyId} style={{borderLeft: "2px solid #2bcbfb", paddingLeft: "10px", cursor:"pointer"}}>
                        <div style={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center",
                            padding: "8px 10px 4px 0",
                        }}>
                            <p style={{color: "#4dd2fa", fontWeight:"bolder", fontSize: "15px"}}>{anomaly.anomalyId}</p>
                            <p style={{color: anomaly.level?.toUpperCase() === "ERROR" ? "#E24B4A" : "#f0a500"}}> • {anomaly.level.toUpperCase()}</p>
                        </div>
                        <p style={{fontSize:"11px", fontFamily: "'JetBrains Mono', monospace",
                        }}>{anomaly.content}</p>
                        <div className={styles.searchDivider}></div>
                    </div>
                ))
            ) : (
                <p>No anomalies found</p>
            )}
        </div>
    );
}


export default AnomalyDisplay