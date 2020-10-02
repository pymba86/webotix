export const formatDate = (timestamp: any) => {
    const d = new Date(timestamp)
    return d.toLocaleDateString() + " " + d.toLocaleTimeString()
}

export const unixToDate = (timestamp: any) => {
    return new Date(timestamp * 1000)
}