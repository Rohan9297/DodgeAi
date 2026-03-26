import { useState, useRef, useEffect } from 'react'
import axios from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

export default function ChatPanel({ selectedNode }) {
  const [messages, setMessages] = useState([
    {
      role: 'assistant',
      text: 'Hi! I can answer questions about your SAP Order-to-Cash data. Try asking: "How many sales orders are there?" or "Which products appear in the most billing documents?"'
    }
  ])
  const [input, setInput] = useState('')
  const [loading, setLoading] = useState(false)
  const bottomRef = useRef(null)

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' })
  }, [messages])

  useEffect(() => {
    if (selectedNode) {
      const props = Object.entries(selectedNode)
        .filter(([k]) => !['id'].includes(k))
        .map(([k, v]) => `${k}: ${v}`)
        .join('\n')
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: `Selected node:\n${props}`,
        isNodeInfo: true
      }])
    }
  }, [selectedNode])

  const sendMessage = async () => {
    if (!input.trim() || loading) return
    const question = input.trim()
    setInput('')
    setMessages(prev => [...prev, { role: 'user', text: question }])
    setLoading(true)

    try {
      const res = await axios.post(`${API_BASE_URL}/api/query`, { question })
      const { answer, sql, relevant } = res.data
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: answer,
        sql: sql,
        relevant
      }])
    } catch (err) {
      setMessages(prev => [...prev, {
        role: 'assistant',
        text: 'Error connecting to backend. Make sure Spring Boot is running.'
      }])
    }
    setLoading(false)
  }

  const handleKey = (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault()
      sendMessage()
    }
  }

  return (
    <div style={{
      display: 'flex', flexDirection: 'column',
      height: '100%', background: '#13151f'
    }}>
      {/* Header */}
      <div style={{
        padding: '12px 16px', borderBottom: '1px solid #2d3154',
        fontSize: 13, fontWeight: 600, color: '#7c8cf8'
      }}>
        Chat with your data
      </div>

      {/* Messages */}
      <div style={{ flex: 1, overflowY: 'auto', padding: '12px 16px' }}>
        {messages.map((msg, i) => (
          <div key={i} style={{ marginBottom: 16 }}>
            <div style={{
              fontSize: 11, color: '#6b7280', marginBottom: 4,
              textAlign: msg.role === 'user' ? 'right' : 'left'
            }}>
              {msg.role === 'user' ? 'You' : 'Dodge AI'}
            </div>
            <div style={{
              background: msg.role === 'user' ? '#2d3154' : '#1a1d2e',
              borderRadius: 8, padding: '10px 12px',
              fontSize: 13, lineHeight: 1.6,
              marginLeft: msg.role === 'user' ? '20%' : 0,
              marginRight: msg.role === 'user' ? 0 : '20%',
              border: '1px solid #2d3154',
              whiteSpace: 'pre-wrap',
              color: msg.isNodeInfo ? '#10b981' : '#e2e8f0'
            }}>
              {msg.text}
            </div>
            {msg.sql && (
              <div style={{
                marginTop: 6, padding: '6px 10px',
                background: '#0f1117', borderRadius: 6,
                fontSize: 10, color: '#6b7280',
                fontFamily: 'monospace', border: '1px solid #2d3154',
                marginRight: '20%'
              }}>
                SQL: {msg.sql}
              </div>
            )}
          </div>
        ))}
        {loading && (
          <div style={{ fontSize: 13, color: '#6b7280', fontStyle: 'italic' }}>
            Thinking...
          </div>
        )}
        <div ref={bottomRef} />
      </div>

      {/* Input */}
      <div style={{
        padding: '12px 16px', borderTop: '1px solid #2d3154',
        display: 'flex', gap: 8
      }}>
        <textarea
          value={input}
          onChange={e => setInput(e.target.value)}
          onKeyDown={handleKey}
          placeholder="Ask about your data..."
          rows={2}
          style={{
            flex: 1, background: '#1a1d2e', border: '1px solid #2d3154',
            borderRadius: 8, padding: '8px 12px', color: '#e2e8f0',
            fontSize: 13, resize: 'none', outline: 'none',
            fontFamily: 'inherit'
          }}
        />
        <button
          onClick={sendMessage}
          disabled={loading}
          style={{
            background: '#7c8cf8', border: 'none', borderRadius: 8,
            padding: '0 16px', color: '#fff', fontSize: 13,
            fontWeight: 600, cursor: loading ? 'not-allowed' : 'pointer',
            opacity: loading ? 0.6 : 1
          }}
        >
          Send
        </button>
      </div>
    </div>
  )
}
