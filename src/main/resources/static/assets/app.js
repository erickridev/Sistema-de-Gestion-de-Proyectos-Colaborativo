import { STATES, escapeHtml as e, initials, progress, filterTasks, filterProjects, passwordValid, dateLabel } from './core.mjs';

const root = document.querySelector('#app');
const dialog = document.querySelector('#editor');
const toast = document.querySelector('#toast');
const titles = { resumen: 'Resumen', proyectos: 'Proyectos', tareas: 'Tareas', categorias: 'Categorías', usuarios: 'Equipo', roles: 'Roles' };
const descriptions = {
  resumen: 'Una mirada a tus proyectos y a lo que sigue.', proyectos: 'Cada gran resultado comienza con un buen plan.',
  tareas: 'Del primer paso a la última entrega. Todo en un solo lugar.', categorias: 'Dale orden y contexto a cada proyecto.',
  usuarios: 'Las personas que hacen que las cosas sucedan.', roles: 'Organiza los roles de tu espacio de trabajo.',
};
const paths = {
  grid: '<rect x="3" y="3" width="7" height="7" rx="1.5"/><rect x="14" y="3" width="7" height="7" rx="1.5"/><rect x="3" y="14" width="7" height="7" rx="1.5"/><rect x="14" y="14" width="7" height="7" rx="1.5"/>',
  folder: '<path d="M3 7a2 2 0 0 1 2-2h5l2 2h7a2 2 0 0 1 2 2v10H5a2 2 0 0 1-2-2z"/>',
  task: '<rect x="4" y="4" width="16" height="17" rx="2"/><path d="M9 4V2h6v2M8 10l2 2 5-5M8 16h8"/>',
  layers: '<path d="m12 3 10 5-10 5L2 8zM2 13l10 5 10-5M2 18l10 5 10-5"/>',
  users: '<circle cx="9" cy="8" r="3"/><path d="M3 21v-3a6 6 0 0 1 12 0v3M17 5a3 3 0 0 1 0 6M18 15a5 5 0 0 1 3 5"/>',
  shield: '<path d="m12 3 8 3v6c0 5-8 9-8 9s-8-4-8-9V6zM9 12l2 2 4-4"/>',
  plus: '<path d="M12 5v14M5 12h14"/>', check: '<path d="m5 12 4 4L19 6"/>',
  search: '<circle cx="10.5" cy="10.5" r="6.5"/><path d="m16 16 5 5"/>',
  calendar: '<rect x="3" y="5" width="18" height="16" rx="2"/><path d="M16 3v4M8 3v4M3 11h18"/>',
  edit: '<path d="m16 3 5 5-12 12-6 1 1-6zM14 5l5 5"/>',
  delete: '<path d="M3 6h18M9 6V3h6v3M5 6l1 15h12l1-15M10 10v7M14 10v7"/>',
  logout: '<path d="M9 3H4v18h5M14 8l5 4-5 4M8 12h11"/>',
  refresh: '<path d="M20 8a8 8 0 1 0 0 8M20 3v5h-5"/>',
  close: '<path d="m6 6 12 12M6 18 18 6"/>', menu: '<path d="M4 6h16M4 12h16M4 18h16"/>',
  leaf: '<path d="M20 3c0 11-1 16-8 16a7 7 0 0 1-7-7c0-7 8-8 15-9zM3 21l12-12"/>',
  clock: '<circle cx="12" cy="12" r="9"/><path d="M12 7v5l3 2"/>',
};
const icon = name => `<svg viewBox="0 0 24 24" aria-hidden="true">${paths[name] || paths.folder}</svg>`;
const brand = () => '<div class="brand"><span class="brand-mark" aria-hidden="true">n</span>nexo<span style="color:#9cb08d">.</span></div>';
const defaults = () => ({ search: '', category: '', project: '', status: '', mine: false });
const mobileLayout = window.matchMedia('(max-width: 760px)');
function storedToken() { try { return sessionStorage.getItem('nexo.token') || ''; } catch { return ''; } }
const state = { token: storedToken(), user: null, projects: [], tasks: [], categories: [], users: [], roles: [],
  view: 'resumen', filters: defaults(), loading: true, refreshing: false, error: '', authMode: 'login' };
const admin = () => state.user?.authorities?.includes('ROLE_ADMIN');
let toastTimer;
let sessionTimer;
let editorContext = null;

function notify(message, error = false) {
  clearTimeout(toastTimer); toast.textContent = message; toast.className = `toast${error ? ' error' : ''}`; toast.hidden = false;
  toastTimer = setTimeout(() => { toast.hidden = true; }, error ? 7000 : 4500);
}

function setToken(token) {
  state.token = token;
  try { token ? sessionStorage.setItem('nexo.token', token) : sessionStorage.removeItem('nexo.token'); } catch { /* La sesion sigue en memoria. */ }
}

async function api(path, { method = 'GET', body, publicRequest = false } = {}) {
  let response;
  try {
    response = await fetch(`/api${path}`, { method, cache: 'no-store', credentials: 'omit',
      headers: { Accept: 'application/json', ...(body !== undefined ? { 'Content-Type': 'application/json' } : {}),
        ...(!publicRequest && state.token ? { Authorization: `Bearer ${state.token}` } : {}) },
      ...(body !== undefined ? { body: JSON.stringify(body) } : {}) });
  } catch { throw new Error('No pudimos conectar con el servidor. Comprueba tu conexión e inténtalo de nuevo.'); }
  let data = null;
  const text = await response.text();
  if (text) { try { data = JSON.parse(text); } catch { /* Se informa el estado HTTP abajo. */ } }
  if (!response.ok) {
    if (response.status === 401 && !publicRequest) { logout(false); notify('Tu sesión terminó. Inicia sesión para continuar.', true); }
    const fallbacks = { 401: 'El correo o la contraseña no son correctos.', 403: 'No tienes permiso para realizar esta acción.',
      404: 'El registro ya no está disponible. Actualiza la página.', 409: 'No se puede completar la operación: el registro está en uso o ya existe.' };
    const error = new Error(data?.message || fallbacks[response.status] || 'No pudimos completar la operación. Inténtalo de nuevo.');
    error.status = response.status; throw error;
  }
  return data;
}

function logout(announce = true) {
  clearTimeout(sessionTimer); setToken(''); state.user = null;
  state.projects = []; state.tasks = []; state.categories = []; state.users = []; state.roles = [];
  state.error = ''; state.refreshing = false; state.loading = false; state.authMode = 'login';
  if (dialog.open) dialog.close(); editorContext = null;
  renderAuth(); if (announce) notify('Sesión cerrada. Hasta pronto.');
}

function scheduleExpiry() {
  clearTimeout(sessionTimer);
  try {
    const base64 = state.token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/');
    const payload = JSON.parse(atob(base64));
    const delay = payload.exp * 1000 - Date.now();
    if (!Number.isFinite(delay) || delay <= 0) { logout(false); return false; }
    sessionTimer = setTimeout(() => { logout(false); notify('Tu sesión expiró. Vuelve a iniciar sesión.', true); }, Math.min(delay, 2147483647));
    return true;
  } catch { logout(false); return false; }
}

async function establishSession(token) {
  setToken(token); const current = token;
  try {
    const user = await api('/auth/me');
    if (state.token !== current) return;
    state.user = user; if (!scheduleExpiry()) return;
    state.view = route(); state.filters = defaults(); await loadData();
  } catch (error) {
    if (state.token === current) { logout(false); notify(error.message, true); }
  }
}

async function loadData() {
  if (!state.user || state.refreshing) return;
  const token = state.token;
  state.loading = true; state.refreshing = true; state.error = ''; renderApp();
  try {
    const urls = ['/proyectos', '/tareas', '/categorias', ...(admin() ? ['/usuarios', '/roles'] : [])];
    const data = await Promise.all(urls.map(url => api(url)));
    if (state.token !== token) return;
    [state.projects, state.tasks, state.categories, state.users = [], state.roles = []] = data;
  } catch (error) {
    if (state.token === token) state.error = error.message;
  } finally {
    if (state.token === token && state.user) { state.loading = false; state.refreshing = false; renderApp(); }
  }
}

function route() {
  const value = location.hash.slice(1);
  return titles[value] && (admin() || !['usuarios', 'roles'].includes(value)) ? value : 'resumen';
}

function renderAuth() {
  const register = state.authMode === 'register';
  document.title = `${register ? 'Crear cuenta' : 'Iniciar sesión'} · Nexo`;
  root.innerHTML = `<main class="auth" id="main">
    <section class="auth-story" aria-label="Nexo, proyectos en equipo">
      ${brand()}<div><span class="eyebrow">Menos pendientes. Más posibilidades.</span>
      <h1>Las grandes ideas<br>se construyen<br><em>en equipo.</em></h1>
      <p>Conecta a las personas, organiza tus proyectos y dale espacio a lo que de verdad importa.</p>
      <div class="story-visual" aria-hidden="true">
        <div><span class="story-check">${icon('check')}</span>Un objetivo compartido</div>
        <div><span class="story-check">${icon('check')}</span>Cada tarea en su lugar</div>
        <div><span class="story-check">${icon('users')}</span>Todo tu equipo, conectado</div>
        <div class="story-line"><span></span></div>
      </div></div><footer>Un espacio para convertir planes en progreso.</footer>
    </section>
    <section class="auth-form-wrap"><form id="auth-form" class="auth-form">
      <span class="eyebrow muted">Tu espacio de trabajo</span><h2 style="margin-top:16px">${register ? 'Empecemos algo bueno.' : 'Qué bueno verte de nuevo.'}</h2>
      <p>${register ? 'Crea tu cuenta y súmate al trabajo en equipo.' : 'Inicia sesión para retomar donde lo dejaste.'}</p>
      <div id="auth-error" class="form-error" role="alert" hidden></div>
      ${register ? `<label class="field"><span>Nombre completo</span><input name="nombre" autocomplete="name" required maxlength="100" placeholder="Tu nombre"></label>
        <label class="field"><span>Nombre de usuario</span><input name="login" autocomplete="username" required maxlength="50" placeholder="Cómo te llamamos"></label>` : ''}
      <label class="field"><span>Correo electrónico</span><input name="email" type="email" autocomplete="${register ? 'email' : 'username'}" required maxlength="100" placeholder="tu@equipo.com"></label>
      <label class="field"><span>Contraseña</span><input name="password" type="password" autocomplete="${register ? 'new-password' : 'current-password'}" required ${register ? 'minlength="8" maxlength="72"' : ''} placeholder="${register ? 'Crea una contraseña segura' : 'Tu contraseña'}">
      ${register ? '<small>Al menos 8 caracteres y un máximo de 72 bytes UTF-8.</small>' : ''}</label>
      <button class="button" type="submit">${register ? 'Crear mi cuenta' : 'Iniciar sesión'}</button>
      <p class="auth-footer">${register ? '¿Ya tienes una cuenta?' : '¿Es tu primera vez aquí?'} <button class="text-button" type="button" data-action="auth-mode">${register ? 'Inicia sesión' : 'Crea tu cuenta'}</button></p>
    </form></section></main>`;
}

function navLink(view, name, count) {
  return `<a class="nav-link${state.view === view ? ' active' : ''}" href="#${view}" ${state.view === view ? 'aria-current="page"' : ''}>${icon(name)}<span>${titles[view]}</span>${count != null ? `<span class="nav-counter">${count}</span>` : ''}</a>`;
}

function renderApp() {
  if (!state.user) return renderAuth();
  document.title = `${titles[state.view]} · Nexo`;
  root.innerHTML = `<div class="layout"><button class="mobile-overlay" data-action="menu" aria-label="Cerrar navegación"></button>
    <aside class="sidebar" ${mobileLayout.matches ? 'inert' : ''}>${brand()}<div class="workspace"><span class="workspace-icon">${icon('layers')}</span><div>Espacio colaborativo<small>Gestión de proyectos</small></div></div>
      <div class="nav-label">Espacio de trabajo</div><nav aria-label="Navegación principal">${navLink('resumen', 'grid')}${navLink('proyectos', 'folder', state.projects.length)}${navLink('tareas', 'task', state.tasks.filter(t => t.estado !== 'COMPLETADA').length)}${navLink('categorias', 'layers')}
      ${admin() ? `<div class="nav-spacer"></div><div class="nav-label">Administración</div>${navLink('usuarios', 'users')}${navLink('roles', 'shield')}` : ''}</nav>
      <div class="sidebar-bottom"><div class="sidebar-tip">${icon('leaf')}<strong>Un paso a la vez.</strong><p>El progreso es la suma de pequeñas tareas terminadas.</p></div>
      <div class="profile"><span class="avatar large">${e(initials(state.user.nombre))}</span><div class="profile-text"><strong>${e(state.user.nombre)}</strong><small>${admin() ? 'Administrador' : 'Miembro del equipo'}</small></div>
      <button class="icon-button" data-action="logout" title="Cerrar sesión" aria-label="Cerrar sesión">${icon('logout')}</button></div></div>
    </aside><div class="content-wrap"><header class="topbar"><div class="breadcrumb"><button class="icon-button mobile-menu" data-action="menu" aria-label="Abrir navegación" aria-expanded="false">${icon('menu')}</button><span>Espacio de trabajo</span><span>/</span><strong>${titles[state.view]}</strong></div>
    <div class="topbar-right"><span class="today">${e(new Intl.DateTimeFormat('es-SV', { weekday: 'long', day: 'numeric', month: 'long' }).format(new Date()))}</span><span class="connection">Sesión activa</span><span class="avatar">${e(initials(state.user.nombre))}</span></div></header>
    <main class="main" id="main" tabindex="-1">${pageHeading()}
    ${state.loading ? '<div class="loading" role="status"><span class="spinner"></span>Cargando tu espacio de trabajo…</div>' : state.error ? `<div class="error-panel" role="alert"><h3>No pudimos cargar el espacio</h3><p>${e(state.error)}</p><button class="button secondary" data-action="refresh">${icon('refresh')}Volver a intentar</button></div>` : pageContent()}
    <footer class="page-footer"><span>Nexo · Cada proyecto, un paso adelante.</span><span>Hecho para trabajar en equipo</span></footer>
    </main></div></div>`;
}

function pageHeading() {
  const entity = { proyectos: 'project', tareas: 'task', categorias: 'category', roles: 'role' }[state.view];
  const labels = { project: 'Nuevo proyecto', task: 'Nueva tarea', category: 'Nueva categoría', role: 'Nuevo rol' };
  return `<div class="page-heading"><div><div class="eyebrow">${state.view === 'resumen' ? 'Todo empieza aquí' : 'Espacio de trabajo'}</div>
    <h1>${state.view === 'resumen' ? `Hola, ${e(state.user.nombre.split(' ')[0])}<span style="color:#91aa73">.</span>` : titles[state.view]}</h1><p>${descriptions[state.view]}</p></div>
    <div class="heading-actions"><button class="icon-button" data-action="refresh" title="Actualizar datos" aria-label="Actualizar datos" ${state.loading ? 'disabled' : ''}>${icon('refresh')}</button>
    ${admin() && (entity || state.view === 'resumen') ? `<button class="button" data-action="create" data-entity="${entity || 'project'}" ${state.loading ? 'disabled' : ''}>${icon('plus')}${labels[entity || 'project']}</button>` : ''}</div></div>`;
}

function pageContent() {
  if (state.view === 'resumen') return dashboard();
  return `${toolbar()}<div id="results">${collection()}</div>`;
}

function dashboard() {
  const all = progress(state.tasks);
  const pending = state.tasks.filter(t => t.estado === 'PENDIENTE').length;
  const ongoing = state.tasks.filter(t => t.estado === 'EN_PROGRESO').length;
  const stats = [['folder', 'Proyectos', state.projects.length, `${state.categories.length} categorías de trabajo`],
    ['task', 'Tareas pendientes', pending, 'Un nuevo paso por dar'], ['clock', 'En progreso', ongoing, 'Ideas que están tomando forma'],
    ['check', 'Completadas', all.done, `${all.percent}% de las tareas terminadas`]];
  return `<section class="welcome"><div><span class="eyebrow">El trabajo fluye mejor, juntos</span><h2>Un buen día para avanzar.</h2><p>Una visión clara de lo que estás construyendo.<br>Un lugar para organizar lo que viene.</p></div>
    <div class="welcome-art" aria-hidden="true"><div class="art-card">${icon('folder')}<span></span><span></span></div><div class="art-card">${icon('check')}<span></span><span></span></div><div class="art-card">${icon('users')}<span></span><span></span></div></div></section>
    <section class="stats" aria-label="Estadísticas del espacio">${stats.map(([name, label, count, detail]) => `<article class="stat"><div class="stat-top">${label}<span class="stat-icon">${icon(name)}</span></div><strong>${count}</strong><small>${detail}</small></article>`).join('')}</section>
    <section><div class="section-heading"><h2>Tus proyectos <span>${state.projects.length}</span></h2><a class="text-button" href="#proyectos">Ver todos los proyectos</a></div><div class="projects-grid">${state.projects.slice().reverse().slice(0, 3).map(projectCard).join('') || empty('folder', 'Aquí comienza tu próximo proyecto', 'Crea una categoría y un proyecto para empezar a organizar el trabajo de tu equipo.', admin() ? 'Crear proyecto' : '', 'project')}</div></section>
    <div class="dashboard-bottom"><section class="panel"><div class="panel-heading"><h2>Lo que sigue</h2><a href="#tareas">Ver tablero</a></div>
    ${state.tasks.filter(t => t.estado !== 'COMPLETADA').slice(0, 5).map(task => `<div class="task-row"><span class="task-dot" aria-hidden="true"></span><div class="task-row-main"><strong>${e(task.titulo)}</strong><small>${e(task.proyecto?.titulo)}</small></div>${statusPill(task.estado)}</div>`).join('') || empty('check', 'Todo al día', 'Las tareas pendientes y en progreso aparecerán aquí.')}</section>
    <section class="panel"><div class="panel-heading"><h2>Así va el equipo</h2><span class="pill gray">${all.total} tareas</span></div><div class="overview-body"><div class="completion-top"><div class="donut" style="--completion:${all.percent}%"><div>${all.percent}%</div></div><p><strong>El progreso se construye.</strong>${all.total ? `${all.done} de ${all.total} tareas completadas.` : 'Cada tarea cuenta. Comienza con la primera.'}</p></div>
      ${[['Completadas', all.done], ['En progreso', ongoing], ['Pendientes', pending]].map(([label, value]) => `<div class="legend"><span><i aria-hidden="true"></i>${label}</span><strong>${value}</strong></div>`).join('')}</div></section></div>`;
}

function statusPill(status) {
  const entry = STATES[status] || { label: status || 'Sin estado', color: 'gray' };
  return `<span class="pill ${entry.color}">${e(entry.label)}</span>`;
}

function empty(name, title, text, label = '', entity = '') {
  return `<div class="empty-state">${icon(name)}<h3>${e(title)}</h3><p>${e(text)}</p>${label ? `<button class="button secondary" data-action="create" data-entity="${entity}">${icon('plus')}${e(label)}</button>` : ''}</div>`;
}

function projectCard(project) {
  const stat = progress(state.tasks, project.id);
  return `<article class="project-card"><div class="project-top"><span class="project-symbol">${icon('folder')}</span><span class="pill gray">${e(project.categoria?.nombre || 'General')}</span></div>
    <h3>${e(project.titulo)}</h3><p class="description">${e(project.descripcion || 'Un nuevo espacio para construir en equipo.')}</p>
    <div class="project-progress"><div class="progress-label"><span>Progreso del proyecto</span><strong>${stat.percent}%</strong></div><div class="progress-track" role="progressbar" aria-label="Progreso de ${e(project.titulo)}" aria-valuenow="${stat.percent}" aria-valuemin="0" aria-valuemax="100"><div class="progress-fill" style="width:${stat.percent}%"></div></div></div>
    <div class="project-bottom"><span class="project-dates" title="Fecha de finalización">${icon('calendar')}${e(dateLabel(project.fechaFin))}</span><span>${stat.done}/${stat.total} tareas</span>
    <div class="project-actions"><button class="icon-button" data-action="project-tasks" data-id="${project.id}" aria-label="Ver tareas de ${e(project.titulo)}" title="Ver tareas">${icon('task')}</button>${admin() ? actionButtons('project', project.id, project.titulo) : ''}</div></div></article>`;
}

function actionButtons(entity, id, name) {
  return `<button class="icon-button" data-action="edit" data-entity="${entity}" data-id="${id}" aria-label="Editar ${e(name)}" title="Editar">${icon('edit')}</button><button class="icon-button delete" data-action="delete" data-entity="${entity}" data-id="${id}" aria-label="Eliminar ${e(name)}" title="Eliminar">${icon('delete')}</button>`;
}

function options(items, selected, label = 'nombre') {
  return items.map(item => `<option value="${item.id}"${String(item.id) === String(selected) ? ' selected' : ''}>${e(item[label])}</option>`).join('');
}

function toolbar() {
  const f = state.filters;
  return `<div class="toolbar"><label class="search-box">${icon('search')}<input type="search" id="search" aria-label="Buscar en ${titles[state.view]}" placeholder="Buscar ${titles[state.view].toLowerCase()}…" value="${e(f.search)}"></label>
    ${state.view === 'proyectos' ? `<select id="filter-category" aria-label="Filtrar por categoría"><option value="">Todas las categorías</option>${options(state.categories, f.category)}</select>` : ''}
    ${state.view === 'tareas' ? `<select id="filter-project" aria-label="Filtrar por proyecto"><option value="">Todos los proyectos</option>${options(state.projects, f.project, 'titulo')}</select><select id="filter-status" aria-label="Filtrar por estado"><option value="">Todos los estados</option>${Object.entries(STATES).map(([key, value]) => `<option value="${key}"${key === f.status ? ' selected' : ''}>${value.label}</option>`).join('')}</select><button class="button ${f.mine ? '' : 'secondary'} small" data-action="mine" aria-pressed="${f.mine}">Mis tareas</button>` : ''}
    <span class="result-count" id="result-count">${resultLabel()}</span></div>`;
}

function itemsForView() {
  if (state.view === 'proyectos') return filterProjects(state.projects, state.filters);
  if (state.view === 'tareas') return filterTasks(state.tasks, state.filters, state.user.id);
  const items = { categorias: state.categories, usuarios: state.users, roles: state.roles }[state.view] || [];
  const q = state.filters.search.toLocaleLowerCase('es').trim();
  return items.filter(item => `${item.nombre} ${item.email || ''} ${item.login || ''}`.toLocaleLowerCase('es').includes(q));
}
function resultCount() { return itemsForView().length; }
function resultLabel() { const count = resultCount(); return `${count} ${count === 1 ? 'resultado' : 'resultados'}`; }
function collection() {
  const items = itemsForView();
  if (state.view === 'tareas') return kanban(items);
  if (!items.length) return empty('search', 'Nada por aquí todavía', state.filters.search || state.filters.category ? 'No hay resultados para estos filtros. Prueba con otra búsqueda.' : 'Los registros de esta sección aparecerán aquí cuando se creen.');
  if (state.view === 'proyectos') return `<div class="projects-grid">${items.map(projectCard).join('')}</div>`;
  if (state.view === 'categorias') return table(['Categoría', 'Proyectos', 'Acciones'], items.map(item => `<tr><td><strong>${e(item.nombre)}</strong></td><td>${state.projects.filter(p => p.categoria?.id === item.id).length} proyectos</td><td><div class="table-actions">${admin() ? actionButtons('category', item.id, item.nombre) : '<span class="muted">Solo lectura</span>'}</div></td></tr>`));
  if (state.view === 'usuarios') return table(['Persona', 'Correo electrónico', 'Rol', 'Acciones'], items.map(item => `<tr><td><div class="person"><span class="avatar">${e(initials(item.nombre))}</span><div><strong>${e(item.nombre)}</strong><small>@${e(item.login)}</small></div></div></td><td>${e(item.email)}</td><td><span class="pill gray">${e(item.rol?.nombre?.replace(/^ROLE_/, ''))}</span></td><td><div class="table-actions">${item.id !== state.user.id ? `<button class="icon-button delete" data-action="delete" data-entity="user" data-id="${item.id}" aria-label="Eliminar ${e(item.nombre)}" title="Eliminar">${icon('delete')}</button>` : '<span class="pill gray">Tú</span>'}</div></td></tr>`));
  return table(['Rol', 'Tipo', 'Acciones'], items.map(item => { const protectedRole = ['ROLE_ADMIN', 'ROLE_USER'].includes(item.nombre); return `<tr><td><strong>${e(item.nombre)}</strong></td><td><span class="pill ${protectedRole ? 'green' : 'gray'}">${protectedRole ? 'Del sistema' : 'Personalizado'}</span></td><td><div class="table-actions">${protectedRole ? '<span class="muted">Protegido</span>' : actionButtons('role', item.id, item.nombre)}</div></td></tr>`; }));
}
function table(headers, rows) {
  const labeledRows = rows.map(row => {
    let column = 0;
    return row.replace(/<td>/g, () => `<td role="cell" data-label="${e(headers[column++])}">`)
      .replace('<tr>', '<tr role="row">');
  });
  return `<div class="panel table-wrap"><table role="table" aria-label="${titles[state.view]}"><thead><tr role="row">${headers.map(title => `<th role="columnheader" scope="col">${title}</th>`).join('')}</tr></thead><tbody>${labeledRows.join('')}</tbody></table></div>`;
}

function kanban(tasks) {
  return `<div class="kanban">${Object.entries(STATES).map(([key, value]) => { const items = tasks.filter(task => task.estado === key); return `<section class="kanban-column" aria-label="${value.label}"><div class="column-title"><span><i aria-hidden="true"></i>${value.label}</span><b>${items.length}</b></div>${items.map(taskCard).join('') || '<div class="column-empty">Sin tareas en esta etapa.<br>Un poco de espacio para lo que viene.</div>'}</section>`; }).join('')}</div>`;
}

function taskCard(task) {
  const person = task.usuarioAsignado;
  return `<article class="task-card"><span class="pill gray">${e(task.proyecto?.titulo)}</span><h3>${e(task.titulo)}</h3>${task.descripcion ? `<p>${e(task.descripcion)}</p>` : ''}
    <div class="task-card-footer"><span class="assignee"><span class="avatar">${person ? e(initials(person.nombre)) : '·'}</span><span>${e(person?.nombre || 'Sin asignar')}</span></span><span>#${task.id}</span></div>
    ${admin() ? `<select data-task-state="${task.id}" aria-label="Estado de ${e(task.titulo)}">${Object.entries(STATES).map(([key, value]) => `<option value="${key}"${task.estado === key ? ' selected' : ''}>${value.label}</option>`).join('')}</select><div class="project-actions">${actionButtons('task', task.id, task.titulo)}</div>` : ''}</article>`;
}

function updateResults() {
  const results = document.querySelector('#results');
  if (results) results.innerHTML = collection();
  const count = document.querySelector('#result-count');
  if (count) count.textContent = resultLabel();
}

const entities = {
  project: { url: '/proyectos', list: 'projects', label: 'proyecto' }, task: { url: '/tareas', list: 'tasks', label: 'tarea' },
  category: { url: '/categorias', list: 'categories', label: 'categoría' }, user: { url: '/usuarios', list: 'users', label: 'usuario' }, role: { url: '/roles', list: 'roles', label: 'rol' },
};
function entityItem(entity, id) { return state[entities[entity].list].find(item => item.id === Number(id)); }
function field(label, name, value = '', attributes = '') { return `<label class="field"><span>${label}</span><input name="${name}" value="${e(value)}" ${attributes}></label>`; }

function openEditor(entity, id = null) {
  if (!admin() || !entities[entity] || entity === 'user') return;
  if (entity === 'project' && !state.categories.length) { notify('Primero crea una categoría para organizar tus proyectos.'); location.hash = 'categorias'; openEditor('category'); return; }
  if (entity === 'task' && !state.projects.length) { notify('Primero necesitas un proyecto para crear tareas.'); location.hash = 'proyectos'; return; }
  const item = id ? entityItem(entity, id) : {};
  if (!item) return notify('Este registro ya no está disponible.', true);
  editorContext = { entity, id, mode: 'save' };
  let fields;
  if (entity === 'project' || entity === 'task') {
    fields = `${field('Título', 'titulo', item.titulo, 'required maxlength="150" placeholder="Dale un nombre claro"')}
      <label class="field"><span>Descripción <span class="muted">(opcional)</span></span><textarea name="descripcion" rows="3" placeholder="Un poco de contexto para el equipo">${e(item.descripcion)}</textarea></label>`;
    if (entity === 'project') fields += `<label class="field"><span>Categoría</span><select aria-label="Categoría" name="categoria" required><option value="">Selecciona una categoría</option>${options(state.categories, item.categoria?.id)}</select></label><div class="form-grid">${field('Fecha de inicio', 'fechaInicio', item.fechaInicio, 'type="date"')}${field('Fecha de finalización', 'fechaFin', item.fechaFin, 'type="date"')}</div>`;
    else fields += `<div class="form-grid"><label class="field"><span>Proyecto</span><select aria-label="Proyecto" name="proyecto" required><option value="">Selecciona un proyecto</option>${options(state.projects, item.proyecto?.id || state.filters.project, 'titulo')}</select></label><label class="field"><span>Estado</span><select aria-label="Estado" name="estado">${Object.entries(STATES).map(([key, value]) => `<option value="${key}"${(item.estado || 'PENDIENTE') === key ? ' selected' : ''}>${value.label}</option>`).join('')}</select></label></div><label class="field"><span>Persona asignada</span><select aria-label="Persona asignada" name="usuarioAsignado"><option value="">Sin asignar</option>${options(state.users, item.usuarioAsignado?.id)}</select></label>`;
  } else fields = field('Nombre', 'nombre', item.nombre, `required maxlength="${entity === 'role' ? 45 : 100}" placeholder="${entity === 'role' ? 'Por ejemplo, REVISOR' : 'Por ejemplo, Desarrollo'}"`)
    + (entity === 'role' ? '<p class="muted" style="font-size:.8rem">Los permisos operativos corresponden a los roles ADMIN y USER. Los roles personalizados se registran para organización.</p>' : '');
  dialog.innerHTML = `<form id="entity-form"><div class="dialog-heading"><div><h2 id="dialog-title">${id ? 'Editar' : 'Crear'} ${entities[entity].label}</h2><p>Los detalles claros hacen más fácil el trabajo en equipo.</p></div><button type="button" class="icon-button" data-close aria-label="Cerrar">${icon('close')}</button></div><div class="dialog-body"><div class="form-error" id="dialog-error" role="alert" hidden></div>${fields}</div><div class="dialog-footer"><button type="button" class="button secondary" data-close>Cancelar</button><button type="submit" class="button">${id ? 'Guardar cambios' : `Crear ${entities[entity].label}`}</button></div></form>`;
  if (!dialog.open) dialog.showModal();
  dialog.querySelector('input, select, textarea')?.focus();
}

function confirmDelete(entity, id) {
  if (!admin()) return;
  const item = entityItem(entity, id); if (!item) return;
  editorContext = { entity, id, mode: 'delete' };
  dialog.innerHTML = `<form id="entity-form"><div class="dialog-heading"><div><h2 id="dialog-title">Eliminar ${entities[entity].label}</h2><p>Esta acción no se puede deshacer.</p></div><button class="icon-button" type="button" data-close aria-label="Cerrar">${icon('close')}</button></div><div class="dialog-body"><div id="dialog-error" class="form-error" role="alert" hidden></div><p class="confirm-message">¿Quieres eliminar <strong>${e(item.titulo || item.nombre)}</strong>?${entity !== 'task' ? ' Si tiene registros asociados, tendrás que resolver esas relaciones primero.' : ''}</p></div><div class="dialog-footer"><button type="button" class="button secondary" data-close>Cancelar</button><button type="submit" class="button danger">Sí, eliminar</button></div></form>`;
  dialog.showModal();
}

root.addEventListener('submit', async event => {
  if (event.target.id !== 'auth-form') return;
  event.preventDefault(); const form = event.target; const values = Object.fromEntries(new FormData(form));
  const button = form.querySelector('[type=submit]'); const errorBox = form.querySelector('#auth-error'); errorBox.hidden = true;
  for (const key of ['email', 'nombre', 'login']) if (key in values) values[key] = values[key].trim();
  if (state.authMode === 'register' && !passwordValid(values.password)) { errorBox.textContent = 'La contraseña debe tener entre 8 y 72 caracteres y no superar 72 bytes UTF-8.'; errorBox.hidden = false; return; }
  button.disabled = true; const label = button.textContent; button.textContent = 'Un momento…';
  try { const data = await api(`/auth/${state.authMode === 'register' ? 'register' : 'login'}`, { method: 'POST', body: values, publicRequest: true }); await establishSession(data.token); }
  catch (error) { errorBox.textContent = error.message; errorBox.hidden = false; }
  finally { button.disabled = false; button.textContent = label; }
});

root.addEventListener('click', event => {
  const button = event.target.closest('[data-action]'); if (!button || button.disabled) return;
  const { action, entity, id } = button.dataset;
  if (action === 'auth-mode') { state.authMode = state.authMode === 'login' ? 'register' : 'login'; renderAuth(); }
  if (action === 'logout') logout();
  if (action === 'refresh') loadData();
  if (action === 'create') openEditor(entity);
  if (action === 'edit') openEditor(entity, Number(id));
  if (action === 'delete') confirmDelete(entity, Number(id));
  if (action === 'menu') {
    const layout = document.querySelector('.layout'); layout.classList.toggle('menu-open');
    const expanded = layout.classList.contains('menu-open');
    document.querySelector('.sidebar').inert = mobileLayout.matches && !expanded;
    const toggle = document.querySelector('.mobile-menu');
    toggle.setAttribute('aria-expanded', expanded); toggle.setAttribute('aria-label', expanded ? 'Cerrar navegación' : 'Abrir navegación');
    document.querySelector('.content-wrap').inert = mobileLayout.matches && expanded;
    if (expanded) document.querySelector('.sidebar .nav-link')?.focus(); else toggle.focus();
  }
  if (action === 'mine') { state.filters.mine = !state.filters.mine; button.classList.toggle('secondary', !state.filters.mine); button.setAttribute('aria-pressed', state.filters.mine); updateResults(); }
  if (action === 'project-tasks') { state.filters = defaults(); state.filters.project = id; state.view = 'tareas'; history.replaceState(null, '', '#tareas'); renderApp(); }
});

root.addEventListener('input', event => {
  if (event.target.id === 'search') { state.filters.search = event.target.value; updateResults(); }
});

root.addEventListener('change', async event => {
  const input = event.target;
  const key = { 'filter-category': 'category', 'filter-project': 'project', 'filter-status': 'status' }[input.id];
  if (key) { state.filters[key] = input.value; updateResults(); }
  if (input.dataset.taskState && admin()) {
    const task = state.tasks.find(item => item.id === Number(input.dataset.taskState)); const previous = task?.estado;
    input.disabled = true;
    try { await api(`/tareas/${input.dataset.taskState}/estado`, { method: 'PATCH', body: { estado: input.value } }); notify('Estado actualizado. Un paso más.'); await loadData(); }
    catch (error) { input.value = previous; notify(error.message, true); }
    finally { input.disabled = false; }
  }
});

dialog.addEventListener('click', event => { if (event.target.closest('[data-close]') && !dialog.querySelector('[type=submit]')?.disabled) dialog.close(); });
dialog.addEventListener('cancel', event => { if (dialog.querySelector('[type=submit]')?.disabled) event.preventDefault(); });
dialog.addEventListener('submit', async event => {
  event.preventDefault(); if (!editorContext || !state.user) return;
  const { entity, id, mode } = editorContext; const form = event.target; const errorBox = form.querySelector('#dialog-error');
  errorBox.hidden = true; const values = Object.fromEntries(new FormData(form)); const config = entities[entity];
  let body;
  if (mode !== 'delete') {
    if (entity === 'project') {
      if (values.fechaInicio && values.fechaFin && values.fechaFin < values.fechaInicio) { errorBox.textContent = 'La fecha de finalización no puede ser anterior a la fecha de inicio.'; errorBox.hidden = false; return; }
      body = { titulo: values.titulo.trim(), descripcion: values.descripcion.trim(), categoria: { id: Number(values.categoria) }, fechaInicio: values.fechaInicio || null, fechaFin: values.fechaFin || null };
    } else if (entity === 'task') body = { titulo: values.titulo.trim(), descripcion: values.descripcion.trim(), estado: values.estado, proyecto: { id: Number(values.proyecto) }, usuarioAsignado: values.usuarioAsignado ? { id: Number(values.usuarioAsignado) } : null };
    else body = { nombre: values.nombre.trim() };
    if ((body.titulo ?? body.nombre) === '') { errorBox.textContent = 'Escribe un nombre o título válido.'; errorBox.hidden = false; return; }
  }
  const button = form.querySelector('[type=submit]'); const label = button.textContent; button.disabled = true; button.textContent = mode === 'delete' ? 'Eliminando…' : 'Guardando…';
  try {
    await api(`${config.url}${id ? `/${id}` : ''}`, { method: mode === 'delete' ? 'DELETE' : id ? 'PUT' : 'POST', ...(body ? { body } : {}) });
    dialog.close(); editorContext = null; notify(mode === 'delete' ? 'Registro eliminado.' : 'Cambios guardados correctamente.'); await loadData();
  } catch (error) { errorBox.textContent = error.message; errorBox.hidden = false; }
  finally { button.disabled = false; button.textContent = label; }
});

window.addEventListener('hashchange', () => { if (state.user) { state.view = route(); state.filters = defaults(); renderApp(); document.querySelector('#main')?.focus({ preventScroll: true }); } });
window.addEventListener('pageshow', () => { if (state.user) scheduleExpiry(); });
mobileLayout.addEventListener('change', () => {
  const sidebar = document.querySelector('.sidebar');
  if (sidebar) sidebar.inert = mobileLayout.matches && !document.querySelector('.layout').classList.contains('menu-open');
  const content = document.querySelector('.content-wrap');
  if (content) content.inert = mobileLayout.matches && document.querySelector('.layout').classList.contains('menu-open');
});
window.addEventListener('keydown', event => {
  if (event.key === 'Escape' && !dialog.open && document.querySelector('.layout.menu-open')) {
    document.querySelector('.mobile-menu').click(); document.querySelector('.mobile-menu').focus();
  }
});
if (state.token) establishSession(state.token); else renderAuth();
