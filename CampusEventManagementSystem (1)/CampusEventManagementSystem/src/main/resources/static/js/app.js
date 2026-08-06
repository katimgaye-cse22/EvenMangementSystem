/* ==========================================================================
   Campus Event Management System — client-side app
   Vanilla JS talking to the /api/** REST endpoints. Every page includes
   this one file; each section below only wires itself up if the DOM it
   needs is present on the current page.
   ========================================================================== */

/* ------------------------------------------------------------ API helper */
const Api = {
  async request(url, options = {}) {
    const opts = {
      method: options.method || 'GET',
      headers: { 'Accept': 'application/json' },
      credentials: 'same-origin',
    };
    if (options.body !== undefined) {
      opts.headers['Content-Type'] = 'application/json';
      opts.body = JSON.stringify(options.body);
    }

    const res = await fetch(url, opts);

    if (res.status === 401) {
      window.location.href = '/login';
      return Promise.reject(new Error('Not authenticated'));
    }

    const contentType = res.headers.get('content-type') || '';
    const data = contentType.includes('application/json') ? await res.json() : await res.text();

    if (!res.ok) {
      let message = (data && data.message) ? data.message : 'Something went wrong. Please try again.';
      if (data && data.errors && typeof data.errors === 'object' && !message.includes(':')) {
        const details = Object.values(data.errors).join('; ');
        if (details) message += ': ' + details;
      }
      const err = new Error(message);
      err.status = res.status;
      err.body = data;
      throw err;
    }
    return data;
  },
  get(url) { return this.request(url); },
  post(url, body) { return this.request(url, { method: 'POST', body }); },
  put(url, body) { return this.request(url, { method: 'PUT', body }); },
  del(url) { return this.request(url, { method: 'DELETE' }); },
};

/* -------------------------------------------------------------- Helpers */
const MONTHS = ['JAN', 'FEB', 'MAR', 'APR', 'MAY', 'JUN', 'JUL', 'AUG', 'SEP', 'OCT', 'NOV', 'DEC'];

function formatStub(dateStr, timeStr) {
  const [y, m, d] = dateStr.split('-').map(Number);
  return {
    month: MONTHS[m - 1],
    day: String(d).padStart(2, '0'),
    time: timeStr ? timeStr.slice(0, 5) : '',
  };
}

function formatDateLong(dateStr) {
  const d = new Date(dateStr + 'T00:00:00');
  return d.toLocaleDateString(undefined, { weekday: 'short', year: 'numeric', month: 'short', day: 'numeric' });
}

function escapeHtml(str) {
  const div = document.createElement('div');
  div.textContent = str == null ? '' : String(str);
  return div.innerHTML;
}

function showMsg(el, message, type = 'error') {
  if (!el) return;
  el.textContent = message;
  el.className = 'form-msg show ' + type;
}

function hideMsg(el) {
  if (!el) return;
  el.className = 'form-msg';
}

/* --------------------------------------------------------- Active navlink */
(function highlightActiveNav() {
  const path = window.location.pathname;
  document.querySelectorAll('.cems-nav .nav-link[data-path]').forEach((link) => {
    if (link.getAttribute('data-path') === path) {
      link.classList.add('active');
    }
  });
})();

/* --------------------------------------------------------------- Logout */
document.querySelectorAll('[data-action="logout"]').forEach((btn) => {
  btn.addEventListener('click', async (e) => {
    e.preventDefault();
    try {
      await fetch('/logout', { method: 'POST', credentials: 'same-origin' });
    } finally {
      sessionStorage.removeItem('cems_role');
      sessionStorage.removeItem('cems_name');
      window.location.href = '/';
    }
  });
});

/* ===========================================================================
   LOGIN PAGE
   =========================================================================== */
const loginForm = document.getElementById('login-form');
if (loginForm) {
  const msgEl = document.getElementById('login-msg');

  loginForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMsg(msgEl);

    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const submitBtn = loginForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
      // Spring Security's default login processing expects form-encoded params,
      // not JSON, so we bypass the Api helper here.
      const body = new URLSearchParams({ email, password });
      const res = await fetch('/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        credentials: 'same-origin',
        body,
      });
      const data = await res.json();

      if (!res.ok || !data.success) {
        showMsg(msgEl, data.message || 'Invalid email or password.');
        submitBtn.disabled = false;
        return;
      }

      sessionStorage.setItem('cems_role', data.role);
      sessionStorage.setItem('cems_name', data.fullName || '');
      window.location.href = data.role === 'ADMIN' ? '/admin-dashboard' : '/dashboard';
    } catch (err) {
      showMsg(msgEl, 'Could not reach the server. Please try again.');
      submitBtn.disabled = false;
    }
  });
}

/* ===========================================================================
   REGISTER PAGE
   =========================================================================== */
const registerForm = document.getElementById('register-form');
if (registerForm) {
  const msgEl = document.getElementById('register-msg');

  registerForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMsg(msgEl);

    const fullName = document.getElementById('fullName').value.trim();
    const studentId = document.getElementById('studentId').value.trim();
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    const confirmPassword = document.getElementById('confirmPassword').value;

    if (password !== confirmPassword) {
      showMsg(msgEl, 'Passwords do not match.');
      return;
    }
    if (password.length < 8) {
      showMsg(msgEl, 'Password must be at least 8 characters.');
      return;
    }

    const submitBtn = registerForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
      await Api.post('/api/auth/register', { fullName, studentId, email, password });
      showMsg(msgEl, 'Account created. Redirecting to login…', 'success');
      setTimeout(() => { window.location.href = '/login'; }, 1200);
    } catch (err) {
      showMsg(msgEl, err.message);
      submitBtn.disabled = false;
    }
  });
}

/* ===========================================================================
   EVENTS BROWSE PAGE  (events.html)
   =========================================================================== */
const eventsList = document.getElementById('events-list');
if (eventsList) {
  const searchInput = document.getElementById('search-input');
  const categorySelect = document.getElementById('category-select');
  const sortSelect = document.getElementById('sort-select');
  const pagination = document.getElementById('events-pagination');

  const state = { page: 0, size: 9, keyword: '', category: '', sortBy: 'eventDate', sortDir: 'asc', myEventIds: new Set(), role: null };

  async function loadMyRegistrationIds() {
    try {
      const me = await Api.get('/api/auth/me');
      state.role = me.role;
      if (me.role === 'STUDENT') {
        const regs = await Api.get('/api/registrations/my');
        state.myEventIds = new Set(regs.map((r) => r.eventId));
      }
    } catch (err) {
      // Not logged in — browsing is still allowed, registration buttons will prompt login.
      state.role = null;
    }
  }

  function ticketCardHtml(ev) {
    const stub = formatStub(ev.eventDate, ev.startTime);
    const seatsClass = ev.full ? 'full' : (ev.availableSeats <= 5 ? 'low' : '');
    const seatsLabel = ev.full ? 'Full' : `${ev.availableSeats} seat${ev.availableSeats === 1 ? '' : 's'} left`;

    let actionHtml;
    if (state.role !== 'STUDENT') {
      actionHtml = `<a href="/login" class="btn btn-outline-ink btn-sm">Log in to register</a>`;
    } else if (state.myEventIds.has(ev.id)) {
      actionHtml = `<button class="btn btn-danger-soft btn-sm" data-cancel="${ev.id}">Cancel registration</button>
                    <span class="badge-category" style="background:var(--pine-bg);color:var(--pine);">Registered</span>`;
    } else if (ev.full) {
      actionHtml = `<button class="btn btn-outline-ink btn-sm" disabled>Event full</button>`;
    } else {
      actionHtml = `<button class="btn btn-brass btn-sm" data-register="${ev.id}">Register</button>`;
    }

    return `
      <div class="ticket" data-event-card="${ev.id}">
        <div class="ticket-stub">
          <span class="stub-month">${stub.month}</span>
          <span class="stub-day">${stub.day}</span>
          <span class="stub-time">${stub.time}</span>
        </div>
        <div class="ticket-perforation"></div>
        <div class="ticket-body">
          <span class="badge-category">${escapeHtml(ev.category)}</span>
          <h3>${escapeHtml(ev.title)}</h3>
          <div class="ticket-meta">
            <span>📍 ${escapeHtml(ev.venue)}</span>
            <span class="badge-seats ${seatsClass}">${seatsLabel} of ${ev.capacity}</span>
          </div>
          <div class="ticket-actions">${actionHtml}</div>
        </div>
      </div>`;
  }

  function renderPagination(pageData) {
    pagination.innerHTML = '';
    if (pageData.totalPages <= 1) return;

    const makeBtn = (label, page, disabled, active) => {
      const li = document.createElement('li');
      li.className = 'page-item' + (disabled ? ' disabled' : '') + (active ? ' active' : '');
      const a = document.createElement('a');
      a.className = 'page-link';
      a.href = '#';
      a.textContent = label;
      a.style.color = active ? '#fff' : 'var(--ink)';
      a.style.background = active ? 'var(--ink)' : 'transparent';
      a.style.borderColor = 'var(--line)';
      if (!disabled) {
        a.addEventListener('click', (e) => { e.preventDefault(); state.page = page; loadEvents(); });
      }
      li.appendChild(a);
      return li;
    };

    pagination.appendChild(makeBtn('‹', state.page - 1, state.page === 0, false));
    for (let i = 0; i < pageData.totalPages; i++) {
      pagination.appendChild(makeBtn(String(i + 1), i, false, i === state.page));
    }
    pagination.appendChild(makeBtn('›', state.page + 1, state.page >= pageData.totalPages - 1, false));
  }

  async function loadEvents() {
    eventsList.innerHTML = `<div class="skeleton" style="height:96px;margin-bottom:1.25rem;"></div>`.repeat(3);
    try {
      const params = new URLSearchParams({
        page: state.page, size: state.size,
        sortBy: state.sortBy, sortDir: state.sortDir,
        ...(state.keyword ? { keyword: state.keyword } : {}),
        ...(state.category ? { category: state.category } : {}),
      });
      const data = await Api.get(`/api/events?${params.toString()}`);

      if (!data.content.length) {
        eventsList.innerHTML = `
          <div class="empty-state">
            <span class="empty-mark">No matches</span>
            <p>No events fit that search. Try a different keyword or clear the filter.</p>
          </div>`;
        pagination.innerHTML = '';
        return;
      }

      eventsList.innerHTML = data.content.map(ticketCardHtml).join('');
      renderPagination(data);
      wireCardActions();
    } catch (err) {
      eventsList.innerHTML = `<div class="empty-state"><span class="empty-mark">Error</span><p>${escapeHtml(err.message)}</p></div>`;
    }
  }

  function wireCardActions() {
    eventsList.querySelectorAll('[data-register]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-register');
        btn.disabled = true;
        try {
          await Api.post(`/api/events/${id}/register`, {});
          state.myEventIds.add(Number(id));
          loadEvents();
        } catch (err) {
          alert(err.message);
          btn.disabled = false;
        }
      });
    });
    eventsList.querySelectorAll('[data-cancel]').forEach((btn) => {
      btn.addEventListener('click', async () => {
        const id = btn.getAttribute('data-cancel');
        if (!confirm('Cancel your registration for this event?')) return;
        btn.disabled = true;
        try {
          await Api.del(`/api/events/${id}/register`);
          state.myEventIds.delete(Number(id));
          loadEvents();
        } catch (err) {
          alert(err.message);
          btn.disabled = false;
        }
      });
    });
  }

  async function loadCategories() {
    try {
      const categories = await Api.get('/api/events/categories');
      categories.forEach((c) => {
        const opt = document.createElement('option');
        opt.value = c;
        opt.textContent = c;
        categorySelect.appendChild(opt);
      });
    } catch (err) { /* non-critical */ }
  }

  let searchTimer;
  searchInput.addEventListener('input', () => {
    clearTimeout(searchTimer);
    searchTimer = setTimeout(() => {
      state.keyword = searchInput.value.trim();
      state.page = 0;
      loadEvents();
    }, 350);
  });
  categorySelect.addEventListener('change', () => {
    state.category = categorySelect.value;
    state.page = 0;
    loadEvents();
  });
  sortSelect.addEventListener('change', () => {
    const [field, dir] = sortSelect.value.split(':');
    state.sortBy = field;
    state.sortDir = dir;
    state.page = 0;
    loadEvents();
  });

  (async () => {
    await loadMyRegistrationIds();
    await loadCategories();
    await loadEvents();
  })();
}

/* ===========================================================================
   DASHBOARD PAGE  (student landing page)
   =========================================================================== */
const dashboardGreeting = document.getElementById('dashboard-greeting');
if (dashboardGreeting) {
  (async () => {
    try {
      const me = await Api.get('/api/auth/me');
      if (me.role === 'ADMIN') { window.location.href = '/admin-dashboard'; return; }
      dashboardGreeting.textContent = `Welcome back, ${me.fullName.split(' ')[0]}`;

      const regs = await Api.get('/api/registrations/my');
      document.getElementById('stat-registered').textContent = regs.length;

      const upcoming = regs
        .filter((r) => true)
        .slice(0, 3);
      const list = document.getElementById('dashboard-upcoming');
      if (!upcoming.length) {
        list.innerHTML = `<div class="empty-state"><span class="empty-mark">Nothing yet</span><p>You haven't registered for any events. <a href="/events">Browse events</a> to get started.</p></div>`;
      } else {
        list.innerHTML = upcoming.map((r) => `
          <div class="ticket" style="margin-bottom:0.75rem;">
            <div class="ticket-body" style="padding:1rem 1.25rem;">
              <h3 style="font-size:1rem;">${escapeHtml(r.eventTitle)}</h3>
              <span class="badge-seats">Registered ${new Date(r.registeredAt).toLocaleDateString()}</span>
            </div>
          </div>`).join('');
      }
    } catch (err) { /* Api.get already redirects to /login on 401 */ }
  })();
}

/* ===========================================================================
   MY EVENTS PAGE
   =========================================================================== */
const myEventsList = document.getElementById('my-events-list');
if (myEventsList) {
  async function loadMyEvents() {
    myEventsList.innerHTML = `<div class="skeleton" style="height:96px;margin-bottom:1.25rem;"></div>`.repeat(2);
    try {
      const regs = await Api.get('/api/registrations/my');
      if (!regs.length) {
        myEventsList.innerHTML = `<div class="empty-state"><span class="empty-mark">Nothing yet</span><p>You haven't registered for any events. <a href="/events">Browse events</a> to get started.</p></div>`;
        return;
      }
      myEventsList.innerHTML = regs.map((r) => `
        <div class="ticket">
          <div class="ticket-body">
            <span class="badge-category" style="background:var(--pine-bg);color:var(--pine);">Registered</span>
            <h3>${escapeHtml(r.eventTitle)}</h3>
            <div class="ticket-meta">
              <span>Registered on ${new Date(r.registeredAt).toLocaleDateString()}</span>
            </div>
            <div class="ticket-actions">
              <button class="btn btn-danger-soft btn-sm" data-cancel="${r.eventId}">Cancel registration</button>
            </div>
          </div>
        </div>`).join('');

      myEventsList.querySelectorAll('[data-cancel]').forEach((btn) => {
        btn.addEventListener('click', async () => {
          if (!confirm('Cancel your registration for this event?')) return;
          btn.disabled = true;
          try {
            await Api.del(`/api/events/${btn.getAttribute('data-cancel')}/register`);
            loadMyEvents();
          } catch (err) {
            alert(err.message);
            btn.disabled = false;
          }
        });
      });
    } catch (err) {
      myEventsList.innerHTML = `<div class="empty-state"><span class="empty-mark">Error</span><p>${escapeHtml(err.message)}</p></div>`;
    }
  }
  loadMyEvents();
}

/* ===========================================================================
   ADMIN DASHBOARD PAGE
   =========================================================================== */
const adminEventsBody = document.getElementById('admin-events-body');
if (adminEventsBody) {
  async function loadPopularEvents() {
    const el = document.getElementById('popular-events-list');
    if (!el) return;
    try {
      const popular = await Api.get('/api/admin/reports/popular-events?limit=5');
      el.innerHTML = popular.length
        ? popular.map((p, i) => `
            <div class="d-flex justify-content-between align-items-center" style="background:var(--paper); border:1px solid var(--line); border-radius:6px; padding:0.5rem 0.9rem;">
              <span><span class="badge-seats" style="margin-right:0.5rem;">#${i + 1}</span>${escapeHtml(p.eventTitle)}</span>
              <span class="badge-category">${p.registrationCount} registered</span>
            </div>`).join('')
        : `<span style="color:var(--slate); font-size:0.9rem;">No registrations yet.</span>`;
    } catch (err) { /* non-critical panel */ }
  }

  async function loadAdminEvents() {
    adminEventsBody.innerHTML = `<tr><td colspan="6"><div class="skeleton" style="height:24px;"></div></td></tr>`;
    try {
      const data = await Api.get('/api/events?size=100');
      document.getElementById('stat-total-events').textContent = data.totalElements;

      if (!data.content.length) {
        adminEventsBody.innerHTML = `<tr><td colspan="6" class="text-center py-4" style="color:var(--slate);">No events yet. Create your first one.</td></tr>`;
        return;
      }

      let totalRegs = 0;
      adminEventsBody.innerHTML = data.content.map((ev) => {
        totalRegs += ev.registeredCount;
        return `
        <tr>
          <td>
            <div style="font-weight:600;">${escapeHtml(ev.title)}</div>
            <span class="badge-category">${escapeHtml(ev.category)}</span>
          </td>
          <td>${formatDateLong(ev.eventDate)}</td>
          <td>${escapeHtml(ev.venue)}</td>
          <td class="badge-seats ${ev.full ? 'full' : ''}">${ev.registeredCount} / ${ev.capacity}</td>
          <td>
            <button class="btn btn-outline-ink btn-sm" data-view-participants="${ev.id}" data-title="${escapeHtml(ev.title)}">Participants</button>
          </td>
          <td class="text-end">
            <a href="/edit-event/${ev.id}" class="btn btn-outline-ink btn-sm">Edit</a>
            <button class="btn btn-danger-soft btn-sm" data-delete="${ev.id}">Delete</button>
          </td>
        </tr>`;
      }).join('');
      document.getElementById('stat-total-registrations').textContent = totalRegs;

      adminEventsBody.querySelectorAll('[data-delete]').forEach((btn) => {
        btn.addEventListener('click', async () => {
          if (!confirm('Delete this event? This cannot be undone.')) return;
          try {
            await Api.del(`/api/events/${btn.getAttribute('data-delete')}`);
            loadAdminEvents();
          } catch (err) { alert(err.message); }
        });
      });

      adminEventsBody.querySelectorAll('[data-view-participants]').forEach((btn) => {
        btn.addEventListener('click', () => openParticipantsModal(
          btn.getAttribute('data-view-participants'),
          btn.getAttribute('data-title'),
        ));
      });
    } catch (err) {
      adminEventsBody.innerHTML = `<tr><td colspan="6" class="text-center py-4" style="color:var(--rust);">${escapeHtml(err.message)}</td></tr>`;
    }
  }

  async function openParticipantsModal(eventId, title) {
    const modalEl = document.getElementById('participants-modal');
    document.getElementById('participants-modal-title').textContent = `Participants — ${title}`;
    document.getElementById('participants-export-link').href = `/api/admin/events/${eventId}/participants/export`;
    const body = document.getElementById('participants-body');
    body.innerHTML = `<tr><td colspan="3">Loading…</td></tr>`;

    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    try {
      const participants = await Api.get(`/api/admin/events/${eventId}/participants`);
      body.innerHTML = participants.length
        ? participants.map((p) => `
            <tr>
              <td>${escapeHtml(p.studentName)}</td>
              <td>${escapeHtml(p.studentIdNumber)}</td>
              <td>${escapeHtml(p.studentEmail)}</td>
            </tr>`).join('')
        : `<tr><td colspan="3" style="color:var(--slate);">No one has registered yet.</td></tr>`;
    } catch (err) {
      body.innerHTML = `<tr><td colspan="3" style="color:var(--rust);">${escapeHtml(err.message)}</td></tr>`;
    }
  }

  loadAdminEvents();
  loadPopularEvents();
}

/* ===========================================================================
   CREATE / EDIT EVENT FORM
   =========================================================================== */
const eventForm = document.getElementById('event-form');
if (eventForm) {
  const msgEl = document.getElementById('event-form-msg');
  const pathParts = window.location.pathname.split('/').filter(Boolean);
  const isEdit = pathParts[0] === 'edit-event';
  const eventId = isEdit ? pathParts[1] : null;

  if (isEdit) {
    (async () => {
      try {
        const ev = await Api.get(`/api/events/${eventId}`);
        document.getElementById('title').value = ev.title;
        document.getElementById('description').value = ev.description || '';
        document.getElementById('category').value = ev.category;
        document.getElementById('venue').value = ev.venue;
        document.getElementById('eventDate').value = ev.eventDate;
        document.getElementById('startTime').value = ev.startTime.slice(0, 5);
        document.getElementById('endTime').value = ev.endTime.slice(0, 5);
        document.getElementById('capacity').value = ev.capacity;
      } catch (err) {
        showMsg(msgEl, 'Could not load this event: ' + err.message);
      }
    })();
  }

  eventForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    hideMsg(msgEl);

    const payload = {
      title: document.getElementById('title').value.trim(),
      description: document.getElementById('description').value.trim(),
      category: document.getElementById('category').value.trim(),
      venue: document.getElementById('venue').value.trim(),
      eventDate: document.getElementById('eventDate').value,
      startTime: document.getElementById('startTime').value,
      endTime: document.getElementById('endTime').value,
      capacity: Number(document.getElementById('capacity').value),
    };

    const submitBtn = eventForm.querySelector('button[type="submit"]');
    submitBtn.disabled = true;

    try {
      if (isEdit) {
        await Api.put(`/api/events/${eventId}`, payload);
      } else {
        await Api.post('/api/events', payload);
      }
      window.location.href = '/admin-dashboard';
    } catch (err) {
      showMsg(msgEl, err.message);
      submitBtn.disabled = false;
    }
  });
}
