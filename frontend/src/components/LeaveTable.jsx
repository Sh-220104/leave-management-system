import React, { useState } from "react";

/**
 * LeaveTable
 *
 * Props:
 *  - leaveRequests : array of LeaveRequestDto objects from the backend
 *  - showActions   : boolean — show Approve / Reject buttons (Manager view)
 *  - onAction      : (leaveId, isApprove, comment) => void
 *  - loading       : boolean — disables buttons while a request is in flight
 */
function LeaveTable({
  leaveRequests = [],
  showActions = false,
  onAction = () => {},
  loading = false,
}) {
  // Per-row comment state: { [leaveId]: string }
  const [comments, setComments] = useState({});

  const handleCommentChange = (id, value) => {
    setComments(prev => ({ ...prev, [id]: value }));
  };

  const handleAction = (id, isApprove) => {
    onAction(id, isApprove, comments[id] || "");
    // Clear comment after action
    setComments(prev => ({ ...prev, [id]: "" }));
  };

  if (!leaveRequests.length) {
    return <p>No leave records found.</p>;
  }

  return (
    <table className="table">
      <thead>
        <tr>
          {/* Show employee name only in manager / admin view */}
          {showActions && <th>Employee</th>}
          <th>Type</th>
          <th>Start Date</th>
          <th>End Date</th>
          <th>Status</th>
          <th>Reason</th>
          <th>Manager Comment</th>
          {showActions && <th>Action</th>}
        </tr>
      </thead>
      <tbody>
        {leaveRequests.map(r => {
          // ── FIX: use leaveTypeName (populated by backend toDto) ──────────────
          const typeName   = r.leaveTypeName || r.leaveType?.type || r.leaveTypeId || "—";
          // ── FIX: status comes from backend DTO now; guard against undefined ──
          const status     = r.status || "UNKNOWN";
          const isPending  = status === "PENDING";

          return (
            <tr key={r.id}>
              {showActions && <td>{r.employeeName || "—"}</td>}
              <td>{typeName}</td>
              <td>{r.startDate}</td>
              <td>{r.endDate}</td>
              <td>
                <span
                  style={{
                    fontWeight: 600,
                    color:
                      status === "APPROVED"
                        ? "#16a34a"
                        : status === "REJECTED"
                        ? "#dc2626"
                        : "#d97706",
                  }}
                >
                  {status}
                </span>
              </td>
              <td>{r.reason || r.notes || "—"}</td>
              <td>{r.managerComment || "—"}</td>

              {showActions && (
                <td style={{ whiteSpace: "nowrap" }}>
                  {/* ── FIX: buttons disabled only when NOT pending or a request is in flight ── */}
                  <button
                    className="btn-approve"
                    onClick={() => handleAction(r.id, true)}
                    disabled={!isPending || loading}
                    title={!isPending ? `Cannot approve — status is ${status}` : "Approve leave"}
                    style={{
                      marginRight: 4,
                      background: !isPending ? "#ccc" : "#16a34a",
                      color: "#fff",
                      border: "none",
                      padding: "4px 10px",
                      borderRadius: 4,
                      cursor: !isPending || loading ? "not-allowed" : "pointer",
                    }}
                  >
                    Approve
                  </button>

                  <button
                    className="btn-reject"
                    onClick={() => handleAction(r.id, false)}
                    disabled={!isPending || loading}
                    title={!isPending ? `Cannot reject — status is ${status}` : "Reject leave"}
                    style={{
                      marginRight: 4,
                      background: !isPending ? "#ccc" : "#dc2626",
                      color: "#fff",
                      border: "none",
                      padding: "4px 10px",
                      borderRadius: 4,
                      cursor: !isPending || loading ? "not-allowed" : "pointer",
                    }}
                  >
                    Reject
                  </button>

                  <input
                    type="text"
                    placeholder="Comment (optional)"
                    value={comments[r.id] || ""}
                    onChange={e => handleCommentChange(r.id, e.target.value)}
                    disabled={!isPending || loading}
                    style={{ marginTop: 2, width: "130px", fontSize: "0.8rem" }}
                  />
                </td>
              )}
            </tr>
          );
        })}
      </tbody>
    </table>
  );
}

export default LeaveTable;
