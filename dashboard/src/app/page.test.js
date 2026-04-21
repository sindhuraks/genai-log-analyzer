import { render, screen, fireEvent, waitFor, within } from "@testing-library/react";
import Home from "./page";
import "@testing-library/jest-dom";

jest.mock("./anomalydisplay", () => {
  return function MockAnomalyDisplay({ onSelect }) {
    return (
      <button onClick={() => onSelect("ANOM-999")}>
        Select Mock Anomaly
      </button>
    );
  };
});

global.fetch = jest.fn();

describe("Home Page", () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  it("updates the log display when an anomaly is selected", async () => {
    const mockLogData = {
      date: "2026-04-19",
      time: "10:00:00",
      level: "ERROR",
      content: "System crash detected",
    };

    const mockRecurrence = { 
      recurrent: false, 
      firstSeen: "2026-04-19 10:00:00" 
    };

    fetch
      .mockResolvedValueOnce({ ok: true, json: async () => mockLogData })
      .mockResolvedValueOnce({ ok: true, json: async () => mockRecurrence });

    render(<Home />);

    const selectButton = screen.getByText("Select Mock Anomaly");
    fireEvent.click(selectButton);

    await screen.findByText(/System crash detected/i);

    const timestamps = screen.getAllByText(/2026-04-19 10:00:00/i);
    expect(timestamps.length).toBeGreaterThanOrEqual(1);

    const logSection = screen.getByText(/ANOMALY LOG/i).closest('div');
    expect(within(logSection).getByText(/2026-04-19 10:00:00/i)).toBeInTheDocument();

    expect(screen.getByText(/\[error\]/i)).toBeInTheDocument();
    
    expect(screen.getByText(/Selected anomaly : ANOM-999/i)).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /analyze/i })).toBeInTheDocument();

    expect(screen.getByRole('button', { name: /run eval/i })).toBeInTheDocument();
  });
});