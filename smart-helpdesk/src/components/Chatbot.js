import { useState, useRef, useEffect } from 'react';
import { analyzeTicket, createTicket } from '../services/api';
import './Chatbot.css';

function Chatbot({ onTicketCreated }) {
  const user = JSON.parse(localStorage.getItem('user') || 'null');
  const isUserModule = user?.role === 'USER';
  const [isOpen, setIsOpen] = useState(false);
  const [messages, setMessages] = useState([
    { text: 'Hi! I\'m your IT support assistant. How can I help you today?', sender: 'bot' }
  ]);
  const [input, setInput] = useState('');
  const [isCreatingTicket, setIsCreatingTicket] = useState(false);
  const messagesEndRef = useRef(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const appendBotMessage = (text) => {
    setMessages((prev) => [...prev, { text, sender: 'bot' }]);
  };

  const handleSend = async (e) => {
    e.preventDefault();
    if (!input.trim() || isCreatingTicket) return;

    const userMessage = input.trim();
    const lower = userMessage.toLowerCase();

    setMessages((prev) => [...prev, { text: userMessage, sender: 'user' }]);
    setInput('');

    if (lower.includes('status') || lower.includes('track')) {
      appendBotMessage('You can check your ticket status in the "My Issues" page from the sidebar.');
      return;
    }

    if (lower.includes('hello') || lower === 'hi' || lower === 'hey') {
      appendBotMessage('Hello! Describe the issue you are facing, and I will create a ticket from this chat.');
      return;
    }

    if (!isUserModule) {
      appendBotMessage('Ticket creation through chat is enabled for users. You can still review and manage tickets from the dashboard pages.');
      return;
    }

    if (userMessage.length < 10) {
      appendBotMessage('Please describe the issue in a little more detail so I can create the right ticket for you.');
      return;
    }

    setIsCreatingTicket(true);
    appendBotMessage('Analyzing your issue and creating a ticket now...');

    try {
      const analysis = await analyzeTicket(userMessage);
      const ticket = await createTicket({
        requesterId: user.id,
        issueType: analysis.suggestedCategory,
        description: userMessage,
        priority: analysis.suggestedPriority
      });

      const duplicateText = analysis.possibleDuplicates?.length
        ? ` Similar tickets found: ${analysis.possibleDuplicates.map((item) => `#${item.id}`).join(', ')}.`
        : '';
      const routingText = ticket.assignedEmployeeName
        ? ` It was routed to ${ticket.assignedEmployeeName}.`
        : ` It was routed to the ${ticket.issueType.toLowerCase()} team queue.`;

      appendBotMessage(
        `Ticket #${ticket.id} has been created. I categorized it as ${ticket.issueType.toLowerCase()} with ${ticket.priority.toLowerCase()} priority.${routingText}${duplicateText} ${analysis.suggestedSolution}`
      );

      if (onTicketCreated) {
        onTicketCreated();
      }
    } catch (error) {
      appendBotMessage(error.message || 'Sorry, there was an error creating your ticket. Please try again.');
    } finally {
      setIsCreatingTicket(false);
    }
  };

  if (!isUserModule) {
    return null;
  }

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
              placeholder="Describe your IT issue here..."
              disabled={isCreatingTicket}
            />
            <button type="submit" disabled={isCreatingTicket}>
              {isCreatingTicket ? 'Creating...' : 'Send'}
            </button>
          </form>
        </div>
      )}
    </>
  );
}

export default Chatbot;
