export const STATES = {
  PENDIENTE: { label: 'Pendiente', color: 'orange' },
  EN_PROGRESO: { label: 'En progreso', color: 'blue' },
  COMPLETADA: { label: 'Completada', color: 'green' },
};

export function escapeHtml(value = '') {
  return String(value ?? '').replace(/[&<>"']/g, character => ({
    '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;',
  })[character]);
}

export function initials(name = '') {
  return name.trim().split(/\s+/).slice(0, 2).map(word => word[0] || '').join('').toUpperCase();
}

export function progress(tasks, projectId) {
  const items = projectId == null ? tasks : tasks.filter(task => task.proyecto?.id === projectId);
  const done = items.filter(task => task.estado === 'COMPLETADA').length;
  return { total: items.length, done, percent: items.length ? Math.round(done / items.length * 100) : 0 };
}

const normalize = value => String(value ?? '').normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('es');

export function filterTasks(tasks, filters, userId) {
  const query = normalize(filters.search).trim();
  return tasks.filter(task => (!filters.project || String(task.proyecto?.id) === filters.project)
    && (!filters.status || task.estado === filters.status)
    && (!filters.mine || task.usuarioAsignado?.id === userId)
    && normalize([task.titulo, task.descripcion, task.proyecto?.titulo, task.usuarioAsignado?.nombre].join(' ')).includes(query));
}

export function filterProjects(projects, filters) {
  const query = normalize(filters.search).trim();
  return projects.filter(project => (!filters.category || String(project.categoria?.id) === filters.category)
    && normalize([project.titulo, project.descripcion, project.categoria?.nombre].join(' ')).includes(query));
}

export function passwordValid(password) {
  return password.length >= 8 && password.length <= 72 && new TextEncoder().encode(password).length <= 72;
}

export function dateLabel(date) {
  if (!date) return 'Sin fecha';
  // Las fechas del proyecto son locales, no instantes UTC.
  return new Intl.DateTimeFormat('es-SV', { day: 'numeric', month: 'short' }).format(new Date(`${date}T12:00:00`));
}
