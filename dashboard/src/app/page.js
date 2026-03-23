import Image from "next/image";
import styles from "./page.module.css";
import AnomalyDisplay from "./anomalydisplay";

export default function Home() {
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
              <AnomalyDisplay />
            </div>
          </div>
          <div className={styles.rightSection}>
          </div>
        </div>
      </main>
    </div>
  );
}
