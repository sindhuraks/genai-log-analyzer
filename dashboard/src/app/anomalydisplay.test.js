import { render, screen, waitFor, fireEvent } from "@testing-library/react";
import AnomalyDisplay from "./anomalydisplay";
import "@testing-library/jest-dom";

global.fetch = jest.fn();

const mockAnomalies = [
  {
    anomalyId: "ANOM-001",
    level: "ERROR",
    content: "Database connection failed",
    node: "Node-A",
    component: "DB",
  },
];

describe("AnomalyDisplay Component", () => {
  beforeEach(() => {
    fetch.mockClear();
  });

  it("renders a list of anomalies after fetching", async () => {
    // Mock the initial list fetch
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockAnomalies,
    });

    // Mock the availability checks for the 3 models
    fetch.mockResolvedValue({
      ok: true,
      json: async () => ({}),
    });

    render(<AnomalyDisplay query="" onSelect={jest.fn()} selectedId={null} />);

    // Check if the anomaly ID appears on screen
    const anomalyItem = await screen.findByText("ANOM-001");
    expect(anomalyItem).toBeInTheDocument();
    
    // Check if the level is rendered in uppercase
    expect(screen.getByText(/ERROR/i)).toBeInTheDocument();
  });

  it("filters anomalies based on the query prop", async () => {
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockAnomalies,
    });

    // Render with a query that won't match
    render(<AnomalyDisplay query="XYZ" onSelect={jest.fn()} selectedId={null} />);

    await waitFor(() => {
      expect(screen.getByText("No matching anomalies")).toBeInTheDocument();
    });
  });

  it("calls onSelect when an anomaly is clicked", async () => {
    const onSelectMock = jest.fn();
    fetch.mockResolvedValueOnce({
      ok: true,
      json: async () => mockAnomalies,
    });

    render(<AnomalyDisplay query="" onSelect={onSelectMock} selectedId={null} />);

    const anomalyItem = await screen.findByText("ANOM-001");
    fireEvent.click(anomalyItem);

    expect(onSelectMock).toHaveBeenCalledWith("ANOM-001");
  });
});