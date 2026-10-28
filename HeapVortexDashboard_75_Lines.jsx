import React, { useEffect, useState } from "react";

const MAX_HEAP = 1024;

export default function HeapVortexDashboard() {
  const [used, setUsed] = useState(420);
  const [gcCount, setGcCount] = useState(12);
  const [threads, setThreads] = useState(26);
  const [active, setActive] = useState(true);
  const [samples, setSamples] = useState([390, 405, 398, 420]);

  useEffect(() => {
    if (!active) return undefined;
    const timer = setInterval(() => {
      setUsed((value) => Math.max(100, Math.min(980, value + Math.round(Math.random() * 30 - 12))));
      setThreads((value) => Math.max(5, value + Math.round(Math.random() * 4 - 2)));
      if (Math.random() > 0.82) setGcCount((value) => value + 1);
      setSamples((values) => [...values.slice(-9), used]);
    }, 2000);
    return () => clearInterval(timer);
  }, [active, used]);

  const percent = Math.min(100, (used / MAX_HEAP) * 100);
  const health = percent >= 90 ? "Critical" : percent >= 80 ? "Warning" : "Healthy";
  const healthColor = percent >= 90 ? "#dc2626" : percent >= 80 ? "#d97706" : "#16a34a";

  const reset = () => {
    setUsed(420);
    setGcCount(12);
    setThreads(26);
    setSamples([390, 405, 398, 420]);
  };

  return (
    <main style={styles.page}>
      <header style={styles.header}>
        <div><p style={styles.eyebrow}>JVM OBSERVABILITY</p>
          <h1>HeapVortex Dashboard</h1>
          <p>Memory utilization and runtime health overview</p>
        </div>
        <button style={styles.button} onClick={() => setActive((value) => !value)}>
          {active ? "Pause updates" : "Resume updates"}
        </button>
      </header>
      <section style={styles.grid}>
        <Metric label="Heap Used" value={`${percent.toFixed(1)}%`} detail={`${used} MB in use`} />
        <Metric label="Maximum Heap" value={`${MAX_HEAP} MB`} detail="Demo capacity" />
        <Metric label="GC Count" value={gcCount} detail="Simulated collections" />
        <Metric label="Live Threads" value={threads} detail="Simulated thread count" />
      </section>
      <section style={styles.panel}>
        <div style={styles.row}><h2>Heap utilization</h2>
          <strong style={{ color: healthColor }}>{health}</strong></div>
        <div style={styles.track}><div style={{ ...styles.fill, width: `${percent}%`, background: healthColor }} /></div>
        <p>{used} MB used of {MAX_HEAP} MB maximum</p>
        <p style={styles.note}>80% warning threshold. High usage is a signal to investigate, not proof of a leak.</p>
      </section>
      <section style={styles.panel}>
        <h2>Recent heap samples</h2>
        <div style={styles.chart}>
          {samples.map((sample, index) => <div key={`${index}-${sample}`} style={styles.column}>
            <div title={`${sample} MB`} style={{ ...styles.bar, height: `${Math.max(8, sample / MAX_HEAP * 150)}px` }} />
            <small>{index + 1}</small>
          </div>)}
        </div>
        <button style={styles.reset} onClick={reset}>Reset demo</button>
      </section>
      <footer style={styles.footer}>Demo values are simulated. Connect a backend API for live JVM telemetry.</footer>
    </main>
  );
}

function Metric({ label, value, detail }) {
  return <article style={styles.card}><span>{label}</span><strong>{value}</strong><small>{detail}</small></article>;
}

const styles = {
  page: { minHeight: "100vh", padding: 26, background: "#f3f6fb", color: "#182238", fontFamily: "Arial, sans-serif" },
  header: { display: "flex", justifyContent: "space-between", alignItems: "center", gap: 16, flexWrap: "wrap", marginBottom: 24 },
  eyebrow: { color: "#5265d8", fontSize: 11, fontWeight: 800, letterSpacing: 2 },
  button: { border: 0, borderRadius: 9, padding: "11px 15px", background: "#5265d8", color: "white", cursor: "pointer" },
  grid: { display: "grid", gridTemplateColumns: "repeat(auto-fit, minmax(180px, 1fr))", gap: 14, marginBottom: 18 },
  card: { background: "white", border: "1px solid #e4e9f2", borderRadius: 14, padding: 18, display: "flex", flexDirection: "column", gap: 10 },
  panel: { background: "white", border: "1px solid #e4e9f2", borderRadius: 14, padding: 20, marginBottom: 18 },
  row: { display: "flex", justifyContent: "space-between", alignItems: "center" },
  track: { height: 10, background: "#edf0f6", borderRadius: 20, overflow: "hidden" },
  fill: { height: "100%", borderRadius: 20, transition: "width .3s ease" },
  note: { background: "#fff7e8", padding: 12, borderRadius: 9, color: "#94631e", fontSize: 12 },
  chart: { display: "flex", alignItems: "flex-end", gap: 12, minHeight: 175, paddingTop: 10 },
  column: { flex: 1, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "flex-end", gap: 6 },
  bar: { width: "70%", maxWidth: 36, minHeight: 8, background: "#697be4", borderRadius: "6px 6px 2px 2px" },
  reset: { marginTop: 14, padding: "9px 13px", border: "1px solid #d9dfeb", borderRadius: 8, background: "white", cursor: "pointer" },
  footer: { color: "#778197", fontSize: 11, textAlign: "center", padding: 12 }
};
