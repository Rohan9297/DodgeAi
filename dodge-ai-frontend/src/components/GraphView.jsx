import { useEffect, useState, useRef } from 'react'
import CytoscapeComponent from 'react-cytoscapejs'
import axios from 'axios'

const nodeColors = {
  Customer: '#10b981',
  SalesOrder: '#7c8cf8',
  SalesOrderItem: '#a78bfa',
  Delivery: '#f59e0b',
  Billing: '#ef4444',
  Payment: '#06b6d4',
  Product: '#ec4899',
}

const stylesheet = [
  {
    selector: 'node',
    style: {
      label: 'data(label)',
      'font-size': '9px',
      'text-valign': 'center',
      'text-halign': 'center',
      'background-color': '#7c8cf8',
      color: '#fff',
      width: 40,
      height: 40,
      'text-wrap': 'wrap',
      'text-max-width': '60px',
      'border-width': 1.5,
      'border-color': '#ffffff22',
    },
  },
  {
    selector: 'node[type = "Customer"]',
    style: { 'background-color': nodeColors.Customer },
  },
  {
    selector: 'node[type = "SalesOrder"]',
    style: { 'background-color': nodeColors.SalesOrder },
  },
  {
    selector: 'node[type = "SalesOrderItem"]',
    style: { 'background-color': nodeColors.SalesOrderItem, width: 28, height: 28 },
  },
  {
    selector: 'node[type = "Delivery"]',
    style: { 'background-color': nodeColors.Delivery },
  },
  {
    selector: 'node[type = "Billing"]',
    style: { 'background-color': nodeColors.Billing },
  },
  {
    selector: 'node[type = "Payment"]',
    style: { 'background-color': nodeColors.Payment },
  },
  {
    selector: 'node[type = "Product"]',
    style: { 'background-color': nodeColors.Product },
  },
  {
    selector: 'node:selected',
    style: {
      'border-width': 3,
      'border-color': '#ffffff',
      width: 55,
      height: 55,
    },
  },
  {
    selector: 'edge',
    style: {
      width: 1,
      'line-color': '#3d4166',
      'target-arrow-color': '#3d4166',
      'target-arrow-shape': 'triangle',
      'curve-style': 'bezier',
      label: 'data(label)',
      'font-size': '7px',
      color: '#6b7280',
    },
  },
]

export default function GraphView({ onNodeSelect }) {
  const [elements, setElements] = useState([])
  const [loading, setLoading] = useState(true)
  const [stats, setStats] = useState({ nodes: 0, edges: 0 })
  const cyRef = useRef(null)

  useEffect(() => {
    axios.get('http://localhost:8080/api/graph')
      .then(res => {
        const { nodes, edges } = res.data
        setElements([...nodes, ...edges])
        setStats({ nodes: nodes.length, edges: edges.length })
        setLoading(false)
      })
      .catch(err => {
        console.error('Failed to load graph:', err)
        setLoading(false)
      })
  }, [])

  const legend = Object.entries(nodeColors).map(([type, color]) => (
    <div key={type} style={{ display: 'flex', alignItems: 'center', gap: 6, marginBottom: 4 }}>
      <div style={{ width: 12, height: 12, borderRadius: '50%', background: color }} />
      <span style={{ fontSize: 11, color: '#9ca3af' }}>{type}</span>
    </div>
  ))

  return (
    <div style={{ position: 'relative', width: '100%', height: '100%', background: '#0f1117' }}>
      {loading && (
        <div style={{
          position: 'absolute', inset: 0, display: 'flex',
          alignItems: 'center', justifyContent: 'center',
          color: '#7c8cf8', fontSize: 16, zIndex: 10
        }}>
          Loading graph...
        </div>
      )}

      {!loading && (
        <CytoscapeComponent
          elements={elements}
          stylesheet={stylesheet}
          layout={{ name: 'cose', animate: false, nodeRepulsion: 8000, idealEdgeLength: 100 }}
          style={{ width: '100%', height: '100%' }}
          cy={(cy) => {
            cyRef.current = cy
            cy.on('tap', 'node', (evt) => {
              const node = evt.target
              onNodeSelect({
                id: node.id(),
                label: node.data('label'),
                type: node.data('type'),
                ...node.data(),
              })
            })
          }}
        />
      )}

      {/* Stats */}
      <div style={{
        position: 'absolute', top: 12, left: 12,
        background: '#1a1d2ecc', borderRadius: 8, padding: '8px 12px',
        border: '1px solid #2d3154'
      }}>
        <div style={{ fontSize: 11, color: '#9ca3af' }}>
          {stats.nodes} nodes · {stats.edges} edges
        </div>
      </div>

      {/* Legend */}
      <div style={{
        position: 'absolute', bottom: 12, left: 12,
        background: '#1a1d2ecc', borderRadius: 8, padding: '10px 14px',
        border: '1px solid #2d3154'
      }}>
        {legend}
      </div>
    </div>
  )
}