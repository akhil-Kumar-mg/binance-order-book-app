import axios from 'axios';

const getCryptoPairs = () => {
    return new Promise((resolve, reject) => {
        axios.get(`http://localhost:8080/cryptoPair`)
            .then(response => {
                resolve(response)
            })
            .catch(response => {
                reject("Api call failed!")
            })
    })
}

const getMarketDepth = (symbol) => {
    return new Promise((resolve, reject) => {
        axios.get(`http://localhost:8080/marketDepth/${symbol}`)
            .then(response => {
                resolve(response)
            })
            .catch(response => {
                reject("Api call failed!")
            })
    })
}

export default {
    getCryptoPairs,
    getMarketDepth
}