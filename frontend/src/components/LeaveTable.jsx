import React, { useState } from "react";

function LeaveTable({ leaveRequests = [], showActions = false, onAction = () => {}, loading = false }) {
  const [comment, setComment] = useState({});
  return (
    <table className="table">
      <thead>
        <tr>
          <th>Type</th>
          <th>Start</th>
          <th>End</th>
          <th>Status</th>
          <th>Reason</th>
          <th>Manager Comment</th>
          {showActions && <th>Action</th>}
        </tr>
      </thead>
      <tbody>
        {leaveRequests.map((r) => (
          <tr key={r.id}>
            <td>{r.leaveType?.type || r.leaveTypeId}</td>
            <td>{r.startDate}</td>
            <td>{r.endDate}</td>
            <td>{r.status}</td>
            <td>{r.reason || r.notes}</td>
            <td>{r.managerComment || ""}</td>
            {showActions && (
              <td>
                <button
                  onClick={() => onAction(r.id, true, comment[r.id] || "")}
                  disabled={r.status !== "PENDING" || loading}
                >Approve</button>
                <button
                  onClick={() => onAction(r.id, false, comment[r.id] || "")}
                  disabled={r.status !== "PENDING" || loading}
                >Reject</button>
                <input
                  type="text"
                  placeholder="Comment"
                  value={comment[r.id] || ""}
                  onChange={e => setComment({ ...comment, [r.id]: e.target.value })}
                  style={{marginTop: 2, width: '80px'}}
                  disabled={loading}
                />
              </td>
            )}
          </tr>
        ))}
      </tbody>
    </table>
  );
}
export default LeaveTable;
