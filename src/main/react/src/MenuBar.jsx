import React from 'react';
import './SideBar.css';  // CSS for styling the sidebar

export default function MenuBar({ isOpen, toggleSidebar }) {
  return (
    <div className={`sidebar ${isOpen ? 'open' : ''}`}>
      <button className="close-btn" onClick={toggleSidebar}>X</button>
      <div className="sidebar-content">
        <h2>Sidebar Content</h2>
        <p>This is a right-hand sliding sidebar.</p>

      </div>
    </div>
  );
}
