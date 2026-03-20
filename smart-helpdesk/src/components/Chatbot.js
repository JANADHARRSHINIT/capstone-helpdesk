import { useState, useRef, useEffect } from 'react';
import { createTicket } from '../services/api';
import './Chatbot.css';

function Chatbot({ onTicketCreated }) {
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { text: 'Hi! I\'m your IT support assistant. How can I help you today?', sender: 'bot' }
  ]);
  const [input, setInput] = useState('');
  const [awaitingTicket, setAwaitingTicket] = useState(false);
  const [ticketData, setTicketData] = useState({});
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const detectIssueType = (text) => {
    const lower = text.toLowerCase();
    if (lower.includes('network') || lower.includes('internet') || lower.includes('wifi') || lower.includes('vpn')) return 'NETWORK';
    if (lower.includes('software') || lower.includes('application') || lower.includes('app') || lower.includes('email')) return 'SOFTWARE';
    if (lower.includes('hardware') || lower.includes('laptop') || lower.includes('printer') || lower.includes('mouse') || lower.includes('keyboard')) return 'HARDWARE';
    return 'SOFTWARE';
  };

  const detectPriority = (text) => {
    const lower = text.toLowerCase();
    if (lower.includes('urgent') || lower.includes('critical') || lower.includes('asap') || lower.includes('immediately')) return 'HIGH';
    if (lower.includes('soon') || lower.includes('important')) return 'MEDIUM';
    return 'LOW';
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim()) return;

    const userMessage = input.trim();
    setMessages((prev) => [...prev, { text: userMessage, sender: 'user' }]);
    setInput('');

    if (awaitingTicket) {
      if (userMessage.toLowerCase() === 'yes') {
        try {
          const user = JSON.parse(localStorage.getItem('user'));
          await createTicket({
            requesterId: user.id,
            issueType: ticketData.issueType,
            description: ticketData.description,
            priority: ticketData.priority
          });
          setMessages((prev) => [...prev, { 
            text: 'Great! Your ticket has been created successfully. You can track it in "My Issues" page.', 
            sender: 'bot' 
          }]);
          if (onTicketCreated) onTicketCreated();
        } catch (error) {
          setMessages((prev) => [...prev, { 
            text: 'Sorry, there was an error creating your ticket. Please try again.', 
            sender: 'bot' 
          }]);
        }
        setAwaitingTicket(false);
        setTicketData({});
      } else {
        setMessages((prev) => [...prev, { 
          text: 'No problem! How else can I help you?', 
          sender: 'bot' 
        }]);
        setAwaitingTicket(false);
        setTicketData({});
      }
      return;
    }

    const lower = userMessage.toLowerCase();
    
    if (lower.includes('issue') || lower.includes('problem') || lower.includes('help') || lower.includes('not working') || lower.includes('error')) {
      const issueType = detectIssueType(userMessage);
      const priority = detectPriority(userMessage);
      
      setTicketData({
        issueType,
        description: userMessage,
        priority
      });
      
      setTimeout(() => {
        setMessages((prev) => [...prev, { 
          text: `I understand you're facing a ${issueType.toLowerCase()} issue. I've detected this as ${priority.toLowerCase()} priority. Would you like me to create a ticket for you? (Reply with "yes" or "no")`, 
          sender: 'bot' 
        }]);
        setAwaitingTicket(true);
      }, 500);
    } else if (lower.includes('status') || lower.includes('track')) {
      setTimeout(() => {
        setMessages((prev) => [...prev, { 
          text: 'You can check your ticket status in the "My Issues" page from the sidebar.', 
          sender: 'bot' 
        }]);
      }, 500);
    } else if (lower.includes('hello') || lower.includes('hi')) {
      setTimeout(() => {
        setMessages((prev) => [...prev, { 
          text: 'Hello! How can I assist you today?', 
          sender: 'bot' 
        }]);
      }, 500);
    } else {
      setTimeout(() => {
        setMessages((prev) => [...prev, { 
          text: 'I can help you with IT issues. Please describe your problem and I\'ll create a ticket for you.', 
          sender: 'bot' 
        }]);
      }, 500);
    }
  };

  return (
    <>
      {!isOpen && (
        <button className="chatbot-button" onClick={() => setIsOpen(true)}>
          &#128172;
        </button>
      )}

      {isOpen && (
        <div className="chatbot-window">
          <div className="chatbot-header">
            <div className="chatbot-header-info">
              <span className="chatbot-status"></span>
              <span>IT Support Bot</span>
            </div>
            <button className="chatbot-close" onClick={() => setIsOpen(false)}>&times;</button>
          </div>

          <div className="chatbot-messages">
            {messages.map((msg, i) => (
              <div key={i} className={`chatbot-message ${msg.sender}`}>
                <div className="message-bubble">{msg.text}</div>
              </div>
            ))}
            <div ref={messagesEndRef} />
          </div>

          <form onSubmit={handleSend} className="chatbot-input">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              placeholder="Type your message..."
            />
            <button type="submit">Send</button>
          </form>
        </div>
      )}
    </>
  );
}

export default Chatbot;
