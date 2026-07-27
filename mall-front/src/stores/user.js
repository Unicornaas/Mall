import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login as loginApi, register as registerApi } from '../api/user'
import router from '../router'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const user = ref(JSON.parse(localStorage.getItem('user') || 'null'))
  const cartVersion = ref(0)

  const isLoggedIn = () => !!token.value

  const handleLogin = async (form) => {
    const res = await loginApi(form)
    token.value = res.data.token
    user.value = res.data.user
    localStorage.setItem('token', res.data.token)
    localStorage.setItem('user', JSON.stringify(res.data.user))
    router.push('/')
    return res
  }

  const handleRegister = async (form) => {
    await registerApi(form)
    router.push('/login')
  }

  const logout = () => {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('user')
    router.push('/login')
  }

  const notifyCartChange = () => {
    cartVersion.value++
  }

  return { token, user, cartVersion, isLoggedIn, handleLogin, handleRegister, logout, notifyCartChange }
})