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
          const createdTicket = await createTicket({
            requesterId: user.id,
            description: ticketData.description
          });
          setMessages((prev) => [...prev, { 
            text: `Ticket #${createdTicket.id} has been created successfully. You can view its status from the My Issues page.`, 
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
      setTicketData({
        description: userMessage
      });
      
      setTimeout(() => {
        setMessages((prev) => [...prev, { 
          text: 'I can create a ticket from that description. Reply with "yes" to continue.', 
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
