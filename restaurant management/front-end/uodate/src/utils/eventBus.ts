export const BUS_EVENTS = {
  REFRESH_DASHBOARD: 'refresh-dashboard'
}

export const emitRefreshDashboard = () => {
  window.dispatchEvent(new Event(BUS_EVENTS.REFRESH_DASHBOARD))
}

export const onRefreshDashboard = (callback: () => void) => {
  window.addEventListener(BUS_EVENTS.REFRESH_DASHBOARD, callback)
}

export const offRefreshDashboard = (callback: () => void) => {
  window.removeEventListener(BUS_EVENTS.REFRESH_DASHBOARD, callback)
}
