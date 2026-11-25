import { test } from 'node:test';
import assert from 'node:assert/strict';
import { filterTasks, filterProjects, progress, passwordValid, escapeHtml } from '../../main/resources/static/assets/core.mjs';

const tasks = [
  { titulo: 'Diseño de interfaz', estado: 'PENDIENTE', proyecto: { id: 1, titulo: 'Portal' }, usuarioAsignado: { id: 4, nombre: 'María' } },
  { titulo: 'Implementar API', estado: 'EN_PROGRESO', proyecto: { id: 1, titulo: 'Portal' }, usuarioAsignado: { id: 5, nombre: 'Pedro' } },
  { titulo: 'Documentación', estado: 'COMPLETADA', proyecto: { id: 2, titulo: 'Interno' }, usuarioAsignado: null },
];

test('combines project, state, assignee and accent insensitive search filters', () => {
  assert.deepEqual(filterTasks(tasks, { project: '1', status: 'PENDIENTE', mine: true, search: 'diseno' }, 4), [tasks[0]]);
  assert.equal(filterTasks(tasks, { mine: true }, 5).length, 1);
  assert.equal(filterTasks(tasks, { project: '2', mine: true }, 4).length, 0);
  assert.equal(filterTasks(tasks, { search: 'maria' }, 4).length, 1);
});

test('computes real overall and per project progress including empty projects', () => {
  assert.deepEqual(progress(tasks), { total: 3, done: 1, percent: 33 });
  assert.deepEqual(progress(tasks, 1), { total: 2, done: 0, percent: 0 });
  assert.deepEqual(progress(tasks, 2), { total: 1, done: 1, percent: 100 });
  assert.deepEqual(progress(tasks, 99), { total: 0, done: 0, percent: 0 });
});

test('filters projects by category and textual context', () => {
  const projects = [{ titulo: 'Diseño', categoria: { id: 1, nombre: 'Producto' } }, { titulo: 'Otro', categoria: { id: 2, nombre: 'Interno' } }];
  assert.deepEqual(filterProjects(projects, { category: '1', search: 'diseno' }), [projects[0]]);
  assert.deepEqual(filterProjects(projects, { category: '1', search: 'interno' }), []);
});

test('validates UTF-8 password length without truncation', () => {
  assert.equal(passwordValid('Clave123'), true);
  assert.equal(passwordValid('a'.repeat(72)), true);
  assert.equal(passwordValid('á'.repeat(36)), true);
  assert.equal(passwordValid('á'.repeat(37)), false);
  assert.equal(passwordValid('corta'), false);
});

test('escapes user content in HTML and attribute contexts', () => {
  assert.equal(escapeHtml('<img src=x onerror="alert(1)">'), '&lt;img src=x onerror=&quot;alert(1)&quot;&gt;');
  assert.equal(escapeHtml("' &"), '&#39; &amp;');
});
