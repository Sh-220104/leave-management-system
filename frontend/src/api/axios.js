import axios from "axios";

const instance = axios.create({
  baseURL: "http://localhost:8080", // backend
});

instance.interceptors.request.use(function(config){
  const token = localStorage.getItem('token');
  if (token){
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

export default instance;
