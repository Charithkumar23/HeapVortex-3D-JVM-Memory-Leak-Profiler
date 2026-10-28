import React, { useEffect, useMemo, useState } from "react";


const dashboardStyles = `
* { box-sizing: border-box; }
.hv-dashboard { min-height: 100vh; padding: 30px; background: #f3f6fb; color: #172033; font-family: Inter, Arial, sans-serif; }
.hv-header { display: flex; justify-content: space-between; gap: 24px; align-items: center; margin: 0 auto 28px; max-width: 1400px; }
.hv-eyebrow { margin: 0 0 8px; color: #5368d8; font-size: 11px; font-weight: 800; letter-spacing: 2px; }
.hv-header h1 { margin: 0; font-size: clamp(28px, 4vw, 40px); letter-spacing: -1.5px; }
.hv-header h1 span { color: #667085; font-weight: 500; }
.hv-subtitle { margin: 10px 0 0; color: #748096; }
.hv-header-actions { display: flex; align-items: center; gap: 10px; flex-wrap: wrap; }
.status-pill, .activity-live { display: inline-flex; align-items: center; gap: 8px; padding: 9px 12px; border-radius: 99px; font-size: 12px; font-weight: 700; }
.status-ok { background: #e4f8ed; color: #16804a; }
.status-warning { background: #fff0df; color: #b65c0b; }
.status-dot { width: 8px; height: 8px; border-radius: 50%; background: currentColor; display: inline-block; }
.hv-button { border: 0; border-radius: 10px; background: #5265d8; color: white; padding: 11px 14px; cursor: pointer; font-weight: 700; }
.hv-button:hover { filter: brightness(.95); }
.hv-button.secondary { color: #38445b; background: white; border: 1px solid #dfe5ef; }
.hv-metric-grid, .hv-content-grid, .hv-activity-panel, .hv-footer { max-width: 1400px; margin-left: auto; margin-right: auto; }
.hv-metric-grid { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; margin-bottom: 18px; }
.metric-card, .hv-panel { background: white; border: 1px solid #e6eaf2; border-radius: 18px; box-shadow: 0 5px 20px #1b2b4a08; }
.metric-card { padding: 21px; }
.metric-card-top { display: flex; justify-content: space-between; align-items: center; color: #737f94; font-size: 13px; }
.metric-indicator { width: 10px; height: 10px; border-radius: 4px; background: #6074e8; }
.metric-purple .metric-indicator { background: #9a70e8; }
.metric-green .metric-indicator { background: #2eaf7d; }
.metric-orange .metric-indicator { background: #e79a42; }
.metric-value { display: block; margin-top: 17px; font-size: 30px; letter-spacing: -1px; }
.metric-detail { margin: 8px 0 0; color: #8a94a6; font-size: 12px; }
.hv-content-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 18px; margin-bottom: 18px; }
.hv-panel { padding: 23px; }
.hv-panel-heading { display: flex; justify-content: space-between; align-items: center; gap: 12px; margin-bottom: 25px; }
.hv-panel-heading h2 { margin: 0; font-size: 18px; }
.hv-panel-heading p { margin: 7px 0 0; color: #8a94a6; font-size: 12px; }
.panel-tag, .sample-count, .activity-label { color: #6475d8; background: #eef0ff; padding: 7px 9px; border-radius: 8px; font-size: 10px; font-weight: 800; white-space: nowrap; }
.usage-row { margin: 20px 0; }
.usage-label-line { display: flex; justify-content: space-between; margin-bottom: 9px; font-size: 13px; }
.usage-label-line strong { color: #4559ce; }
.usage-track { height: 9px; overflow: hidden; background: #edf0f6; border-radius: 20px; }
.usage-fill { height: 100%; background: linear-gradient(90deg, #6075e8, #9b8bf2); border-radius: inherit; transition: width .35s ease; }
.usage-row small { display: block; margin-top: 8px; color: #8a94a6; font-size: 11px; }
.hv-note { display: flex; flex-direction: column; gap: 6px; padding: 13px; border-radius: 12px; background: #fff7e9; color: #98621c; font-size: 12px; }
.hv-note span { color: #9c835e; line-height: 1.5; }
.history-chart { height: 210px; display: flex; align-items: flex-end; gap: 7px; padding: 12px 4px 0; border-bottom: 1px solid #e7ebf2; }
.history-column { flex: 1; min-width: 0; height: 100%; display: flex; flex-direction: column; justify-content: flex-end; align-items: center; gap: 7px; }
.history-bar { width: min(100%, 24px); min-height: 4px; background: linear-gradient(180deg, #8290f1, #5265d8); border-radius: 6px 6px 2px 2px; transition: height .3s ease; }
.history-column small { font-size: 9px; color: #98a1b2; writing-mode: vertical-rl; transform: rotate(180deg); max-height: 42px; }
.empty-history { align-self: center; width: 100%; text-align: center; color: #9aa3b4; font-size: 13px; }
.chart-footer { display: flex; justify-content: space-between; color: #98a1b2; font-size: 10px; padding-top: 10px; }
.activity-live { background: #e7f8ef; color: #16804a; }
.activity-list { display: flex; flex-direction: column; }
.activity-item { display: flex; align-items: center; gap: 13px; padding: 15px 0; border-top: 1px solid #edf0f5; }
.activity-icon { display: grid; place-items: center; width: 36px; height: 36px; flex: 0 0 36px; border-radius: 11px; background: #eef0ff; color: #5265d8; font-size: 11px; font-weight: 800; }
.activity-item div { flex: 1; min-width: 0; }
.activity-item strong { font-size: 13px; }
.activity-item p { margin: 5px 0 0; color: #8a94a6; font-size: 12px; }
.activity-item time { color: #8a94a6; font-size: 11px; white-space: nowrap; }
.hv-footer { display: flex; justify-content: space-between; gap: 12px; padding: 20px 2px 0; color: #98a1b2; font-size: 11px; }
@media (max-width: 950px) { .hv-header { align-items: flex-start; flex-direction: column; } .hv-metric-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); } .hv-content-grid { grid-template-columns: 1fr; } }
@media (max-width: 560px) { .hv-dashboard { padding: 16px; } .hv-metric-grid { grid-template-columns: 1fr; } .hv-panel { padding: 17px; } .hv-footer { flex-direction: column; } .activity-item time { display: none; } }

.hv-diagnostics-summary { max-width: 1400px; margin: 0 auto 18px; }
.hv-diagnostic-badge { padding: 8px 11px; border-radius: 99px; font-size: 11px; font-weight: 800; }
.hv-diagnostic-healthy { color: #147849; background: #e2f8ec; }
.hv-diagnostic-warning { color: #a65b0a; background: #fff0d9; }
.hv-diagnostic-critical { color: #b42318; background: #fee4e2; }
.hv-diagnostic-unknown { color: #667085; background: #eef0f4; }
.hv-diagnostic-grid { display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; }
.hv-diagnostic-item { display: flex; flex-direction: column; gap: 7px; padding: 16px; border: 1px solid #edf0f6; border-radius: 12px; }
.hv-diagnostic-item span, .hv-diagnostic-item small { color: #8791a4; font-size: 11px; }
.hv-diagnostic-item strong { color: #25324b; font-size: 23px; }
.hv-diagnostic-footnote { margin: 15px 0 0; color: #8a94a6; font-size: 11px; line-height: 1.6; }
@media (max-width: 560px) { .hv-diagnostic-grid { grid-template-columns: 1fr; } }

`;

const initialMetrics = {
  heapUsed: 384,
  heapCommitted: 768,
  heapMax: 1024,
  nonHeapUsed: 92,
  gcCount: 14,
  gcTimeMs: 126,
  threads: 28,
  timestamp: new Date().toISOString(),
};

function formatBytes(value) {
  if (!Number.isFinite(value) || value < 0) return "N/A";
  if (value < 1024) return `${value} MB`;
  return `${(value / 1024).toFixed(2)} GB`;
}

function MetricCard({ title, value, detail, tone = "blue" }) {
  return (
    <article className={`metric-card metric-${tone}`}>
      <div className="metric-card-top">
        <span className="metric-title">{title}</span>
        <span className="metric-indicator" aria-hidden="true" />
      </div>
      <strong className="metric-value">{value}</strong>
      <p className="metric-detail">{detail}</p>
    </article>
  );
}

function UsageBar({ label, used, maximum }) {
  const percentage = maximum > 0 ? Math.min((used / maximum) * 100, 100) : 0;
  return (
    <div className="usage-row">
      <div className="usage-label-line">
        <span>{label}</span>
        <strong>{percentage.toFixed(1)}%</strong>
      </div>
      <div
        className="usage-track"
        role="progressbar"
        aria-label={`${label} utilization`}
        aria-valuenow={Math.round(percentage)}
        aria-valuemin={0}
        aria-valuemax={100}
      >
        <div className="usage-fill" style={{ width: `${percentage}%` }} />
      </div>
      <small>{formatBytes(used)} used of {formatBytes(maximum)}</small>
    </div>
  );
}


function getHeapHealth(used, maximum) {
  if (!Number.isFinite(maximum) || maximum <= 0) return { label: "Unknown", tone: "unknown", percentage: 0 };
  const percentage = Math.min(100, Math.max(0, (used / maximum) * 100));
  if (percentage >= 90) return { label: "Critical", tone: "critical", percentage };
  if (percentage >= 80) return { label: "Warning", tone: "warning", percentage };
  return { label: "Healthy", tone: "healthy", percentage };
}
function formatDuration(milliseconds) {
  if (!Number.isFinite(milliseconds) || milliseconds < 0) return "N/A";
  return milliseconds < 1000 ? `${Math.round(milliseconds)} ms` : `${(milliseconds / 1000).toFixed(2)} s`;
}
function DiagnosticsSummary({ metrics }) {
  const health = getHeapHealth(metrics.heapUsed, metrics.heapMax);
  return (
    <section className="hv-panel hv-diagnostics-summary">
      <div className="hv-panel-heading">
        <div><h2>Diagnostics summary</h2><p>Quick interpretation of the latest demo sample</p></div>
        <span className={`hv-diagnostic-badge hv-diagnostic-${health.tone}`}>{health.label}</span>
      </div>
      <div className="hv-diagnostic-grid">
        <div className="hv-diagnostic-item"><span>Heap pressure</span><strong>{health.percentage.toFixed(1)}%</strong><small>of configured maximum</small></div>
        <div className="hv-diagnostic-item"><span>GC elapsed time</span><strong>{formatDuration(metrics.gcTimeMs)}</strong><small>cumulative demo counter</small></div>
        <div className="hv-diagnostic-item"><span>Thread count</span><strong>{metrics.threads}</strong><small>simulated active threads</small></div>
      </div>
      <p className="hv-diagnostic-footnote">Dashboard metrics are simulated locally. Connect a JVM telemetry endpoint before using these values for operational decisions.</p>
    </section>
  );
}

export default function HeapVortexDashboard() {
  const [metrics, setMetrics] = useState(initialMetrics);
  const [history, setHistory] = useState([]);
  const [isMonitoring, setIsMonitoring] = useState(true);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  useEffect(() => {
    if (!isMonitoring) return undefined;

    const timer = window.setInterval(() => {
      setMetrics((previous) => {
        const nextUsed = Math.max(
          120,
          Math.min(previous.heapMax * 0.96, previous.heapUsed + (Math.random() * 36 - 14))
        );
        const next = {
          ...previous,
          heapUsed: Math.round(nextUsed),
          gcCount: previous.gcCount + (Math.random() > 0.82 ? 1 : 0),
          gcTimeMs: previous.gcTimeMs + (Math.random() > 0.82 ? Math.round(2 + Math.random() * 8) : 0),
          threads: Math.max(5, previous.threads + Math.round(Math.random() * 4 - 2)),
          timestamp: new Date().toISOString(),
        };
        setHistory((items) => [
          ...items.slice(-19),
          { time: new Date().toLocaleTimeString(), used: next.heapUsed },
        ]);
        setLastRefresh(new Date());
        return next;
      });
    }, 2000);

    return () => window.clearInterval(timer);
  }, [isMonitoring]);

  const heapPercent = useMemo(
    () => metrics.heapMax > 0 ? (metrics.heapUsed / metrics.heapMax) * 100 : 0,
    [metrics.heapUsed, metrics.heapMax]
  );
  const alertState = heapPercent >= 80 ? "High usage" : "Normal";

  function resetDemo() {
    setMetrics({ ...initialMetrics, timestamp: new Date().toISOString() });
    setHistory([]);
    setLastRefresh(new Date());
  }

  return (
    <main className="hv-dashboard">
      <style>{dashboardStyles}</style>
      <header className="hv-header">
        <div>
          <p className="hv-eyebrow">JVM OBSERVABILITY</p>
          <h1>HeapVortex <span>Dashboard</span></h1>
          <p className="hv-subtitle">Heap usage, garbage collection, and runtime health at a glance.</p>
        </div>
        <div className="hv-header-actions">
          <span className={`status-pill ${heapPercent >= 80 ? "status-warning" : "status-ok"}`}>
            <span className="status-dot" /> {alertState}
          </span>
          <button className="hv-button secondary" onClick={resetDemo}>Reset demo</button>
          <button className="hv-button" onClick={() => setIsMonitoring((value) => !value)}>
            {isMonitoring ? "Pause updates" : "Resume updates"}
          </button>
        </div>
      </header>

      <section className="hv-metric-grid" aria-label="Runtime metrics">
        <MetricCard title="Heap Used" value={`${heapPercent.toFixed(1)}%`} detail={`${metrics.heapUsed} MB currently used`} tone="blue" />
        <MetricCard title="Heap Committed" value={`${metrics.heapCommitted} MB`} detail="Memory committed by the JVM" tone="purple" />
        <MetricCard title="Garbage Collections" value={metrics.gcCount} detail={`${metrics.gcTimeMs} ms total collection time`} tone="green" />
        <MetricCard title="Live Threads" value={metrics.threads} detail="Simulated runtime thread count" tone="orange" />
      </section>

      <DiagnosticsSummary metrics={metrics} />

      <section className="hv-content-grid">
        <article className="hv-panel">
          <div className="hv-panel-heading">
            <div>
              <h2>Memory utilization</h2>
              <p>Current memory pool overview</p>
            </div>
            <span className="panel-tag">LIVE DEMO</span>
          </div>
          <UsageBar label="Java heap" used={metrics.heapUsed} maximum={metrics.heapMax} />
          <UsageBar label="Non-heap memory" used={metrics.nonHeapUsed} maximum={256} />
          <div className="hv-note">
            <strong>Threshold: 80%</strong>
            <span>High utilization is a signal to investigate, not proof of a memory leak.</span>
          </div>
        </article>

        <article className="hv-panel">
          <div className="hv-panel-heading">
            <div>
              <h2>Heap sample history</h2>
              <p>Recent simulated readings</p>
            </div>
            <span className="sample-count">{history.length} samples</span>
          </div>
          <div className="history-chart" role="img" aria-label="Bar chart of recent heap usage samples">
            {history.length === 0 ? (
              <div className="empty-history">Waiting for the first sample…</div>
            ) : history.map((sample, index) => (
              <div className="history-column" key={`${sample.time}-${index}`} title={`${sample.time}: ${sample.used} MB`}>
                <div className="history-bar" style={{ height: `${Math.max(4, (sample.used / metrics.heapMax) * 100)}%` }} />
                <small>{sample.time}</small>
              </div>
            ))}
          </div>
          <div className="chart-footer">
            <span>Lower usage</span>
            <span>Higher usage</span>
          </div>
        </article>
      </section>

      <section className="hv-panel hv-activity-panel">
        <div className="hv-panel-heading">
          <div>
            <h2>Runtime activity</h2>
            <p>Dashboard status and latest sample</p>
          </div>
          <span className="activity-live"><span className="status-dot" /> {isMonitoring ? "Monitoring active" : "Monitoring paused"}</span>
        </div>
        <div className="activity-list">
          <div className="activity-item">
            <span className="activity-icon">M</span>
            <div><strong>Heap sample refreshed</strong><p>Latest sample: {new Date(metrics.timestamp).toLocaleString()}</p></div>
            <time>{lastRefresh.toLocaleTimeString()}</time>
          </div>
          <div className="activity-item">
            <span className="activity-icon">GC</span>
            <div><strong>Garbage collector metrics</strong><p>{metrics.gcCount} collections recorded in this demo session</p></div>
            <span className="activity-label">Metrics</span>
          </div>
          <div className="activity-item">
            <span className="activity-icon">!</span>
            <div><strong>Leak investigation guidance</strong><p>Compare trends under a repeatable workload and inspect heap dumps.</p></div>
            <span className="activity-label">Info</span>
          </div>
        </div>
      </section>

      <footer className="hv-footer">
        <span>HeapVortex · JVM Memory Leak Profiler</span>
        <span>Demo values are simulated; connect a backend for real JVM telemetry.</span>
      </footer>
    </main>
  );
}
