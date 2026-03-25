import { useState } from 'react'
import GraphView from './components/GraphView'
import ChatPanel from './components/ChatPanel'
import './App.css'

function App() {
  const [selectedNode, setSelectedNode] = useState(null)

  return (
    <div className="app-container">
      <div className="header">
        <h1>Dodge AI — Order to Cash Explorer</h1>
      </div>
      <div className="main-content">
        <div className="graph-section">
          <GraphView onNodeSelect={setSelectedNode} />
        </div>
        <div className="chat-section">
          <ChatPanel selectedNode={selectedNode} />
        </div>
      </div>
    </div>
  )
}

export default App