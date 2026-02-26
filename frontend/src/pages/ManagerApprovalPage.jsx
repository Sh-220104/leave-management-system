import React, { useEffect, useState } from "react";
import LeaveTable from "../components/LeaveTable";
import axios from "../api/axios"; // ← FIX: correct path to the axios instance with JWT interceptor

function ManagerApprovalPage() {
  const [pending, setPending] = useState([]);
  const [loading, setLoading] = useState(false);
  const [resultMsg, setResultMsg] = useState("");

  // ── Fetch all pending leave requests ────────────────────────────────────────
  const fetchPendingLeaves = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/leaves/pending");
      setPending(res.data);
    } catch (err) {
      console.error("Failed to fetch pending leaves:", err);
      setPending([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchPendingLeaves();
  }, []);

  // ── Approve / Reject handler ─────────────────────────────────────────────────
  // isApprove: true  → PUT /leaves/{id}/approve
  // isApprove: false → PUT /leaves/{id}/reject
  // The backend accepts an optional JSON body: { "comment": "..." }
  const handleDecision = async (leaveId, isApprove, managerComment = "") => {
    setLoading(true);
    setResultMsg("");

    const action = isApprove ? "approve" : "reject";
    // Build request body — only include comment when non-empty
    const body = managerComment.trim() ? { comment: managerComment.trim() } : {};

    try {
      await axios.put(`/leaves/${leaveId}/${action}`, body);

      const successMsg = isApprove ? "✅ Leave Approved!" : "❌ Leave Rejected!";
      setResultMsg(successMsg);

      // Optimistically remove the acted-upon request from the pending list
      // then re-fetch to stay in sync with the backend
      setPending(prev => prev.filter(l => l.id !== leaveId));
      await fetchPendingLeaves();
    } catch (error) {
      const serverMsg =
        error.response?.data?.message ||
        error.response?.data ||
        "An error occurred while updating the leave request.";
      setResultMsg(`⚠️ Error: ${serverMsg}`);
      console.error("Decision error:", error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="dashboard-section">
      <h2>Pending Leave Requests</h2>

      {resultMsg && (
        <div
          style={{
            color: resultMsg.startsWith("⚠️") ? "red" : "green",
            marginBottom: 12,
            fontWeight: 600,
          }}
        >
          {resultMsg}
        </div>
      )}

      {loading ? (
        <div>Loading...</div>
      ) : pending.length > 0 ? (
        <LeaveTable
          leaveRequests={pending}
          showActions
          onAction={handleDecision}
          loading={loading}
        />
      ) : (
        <div style={{ color: "#666", marginTop: 12 }}>
          No pending leave requests.
        </div>
      )}
    </div>
  );
}

export default ManagerApprovalPage;
