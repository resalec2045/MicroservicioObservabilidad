const request = require('supertest');

describe('api-gateway basic', () => {
  test('smoke: module loads', async () => {
    // simplemente requerimos el index para comprobar que no lanza al cargar
    const mod = require('../index');
    expect(mod).toBeDefined();
  });
});
