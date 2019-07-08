import React, { Component } from "react";
import { withStyles } from "@material-ui/core/styles";
import { styles } from "./AppStyle";
import Table from "@material-ui/core/Table";
import TableBody from "@material-ui/core/TableBody";
import TableCell from "@material-ui/core/TableCell";
import TableHead from "@material-ui/core/TableHead";
import TableRow from "@material-ui/core/TableRow";
import Paper from "@material-ui/core/Paper";
import Button from "@material-ui/core/Button";
import SockJsClient from "react-stomp";
import AppBar from "@material-ui/core/AppBar";
import Toolbar from "@material-ui/core/Toolbar";
import Grid from "@material-ui/core/Grid";
import Select from "@material-ui/core/Select";
import MenuItem from "@material-ui/core/MenuItem";
import ApiService from "./service/ApiService";

import "./App.css";

class App extends Component {
  state = {
    symbol: "",
    selectedSymbol: {
      symbol: "Symbol 1"
    },
    cryptoPairs: [],
    marketDepthAsks: {},
    marketDepthBids: {},
    webSocketRenderingStarted: false
  };

  componentDidMount() {
    ApiService.getCryptoPairs().then(response => {
      this.setState({
        cryptoPairs: response.data,
        selectedSymbol: response.data[0]
      });
      ApiService.getMarketDepth(response.data[0].symbol).then(response => {
        this.setState({
          marketDepthAsks: response.data.marketDepthForAsks,
          marketDepthBids: response.data.marketDepthForBids,
          webSocketRenderingStarted: true
        });
      });
    });
  }

  handleChange = (event, data) => {
    const { cryptoPairs } = this.state;
    ApiService.getMarketDepth(cryptoPairs[data.key].symbol).then(response => {
      this.setState({
        marketDepthAsks: response.data.marketDepthForAsks,
        marketDepthBids: response.data.marketDepthForBids,
        selectedSymbol: cryptoPairs[data.key]
      });
    });
  };

  render() {
    const classes = this.props.classes;
    const {
      cryptoPairs,
      selectedSymbol,
      marketDepthAsks,
      marketDepthBids,
      webSocketRenderingStarted
    } = this.state;
    return (
      <div>
        <AppBar position="static" color="default">
          <Toolbar
            style={{ margin: "auto", paddingLeft: 30, paddingRight: 30 }}
          >
            <span style={{ paddingRight: 10, fontSize: 20 }}>
              Crypto Pair :{" "}
            </span>
            <Select value={selectedSymbol.symbol} onChange={this.handleChange}>
              {cryptoPairs.map((crypto, index) => {
                return (
                  <MenuItem key={index} value={crypto.symbol}>
                    {crypto.symbol}
                  </MenuItem>
                );
              })}
            </Select>
          </Toolbar>
        </AppBar>

        <Grid container spacing={3} className="mainGrid">
          <Grid item xs={6} className="subGrid">
            <Button variant="contained" className={classes.button}>
              Asks
            </Button>
            <div className="table_box">
              <div className={classes.root}>
                <Paper className={classes.paper}>
                  <Table className={classes.table}>
                    <TableHead>
                      <TableRow>
                        <TableCell>
                          Price(
                          {selectedSymbol.symbol.substr(
                            selectedSymbol.symbol.length - 3,
                            selectedSymbol.symbol.length
                          )}
                          )
                        </TableCell>
                        <TableCell align="right">
                          Amount(
                          {selectedSymbol.symbol.substr(
                            0,
                            selectedSymbol.symbol.length - 3
                          )}
                          )
                        </TableCell>
                        <TableCell align="right">
                          Total(
                          {selectedSymbol.symbol.substr(
                            selectedSymbol.symbol.length - 3,
                            selectedSymbol.symbol.length
                          )}
                          )
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.keys(marketDepthAsks).map((row, index) => {
                        return (
                          <TableRow key={index}>
                            <TableCell component="th" scope="row">
                              {row}
                            </TableCell>
                            <TableCell align="right">
                              {marketDepthAsks[row]}
                            </TableCell>
                            <TableCell align="right">
                              {row * marketDepthAsks[row]}
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </Paper>
              </div>
            </div>
          </Grid>
          <Grid item xs={6}>
            <Button variant="contained" className={classes.button}>
              Bids
            </Button>
            <div className="table_box">
              <div className={classes.root}>
                <Paper className={classes.paper}>
                  <Table className={classes.table}>
                    <TableHead>
                      <TableRow>
                        <TableCell>
                          Price(
                          {selectedSymbol.symbol.substr(
                            selectedSymbol.symbol.length - 3,
                            selectedSymbol.symbol.length
                          )}
                          )
                        </TableCell>
                        <TableCell align="right">
                          Amount(
                          {selectedSymbol.symbol.substr(
                            0,
                            selectedSymbol.symbol.length - 3
                          )}
                          )
                        </TableCell>
                        <TableCell align="right">
                          Total(
                          {selectedSymbol.symbol.substr(
                            selectedSymbol.symbol.length - 3,
                            selectedSymbol.symbol.length
                          )}
                          )
                        </TableCell>
                      </TableRow>
                    </TableHead>
                    <TableBody>
                      {Object.keys(marketDepthBids).map((row, index) => {
                        return (
                          <TableRow key={index}>
                            <TableCell component="th" scope="row">
                              {row}
                            </TableCell>
                            <TableCell align="right">
                              {marketDepthBids[row]}
                            </TableCell>
                            <TableCell align="right">
                              {row * marketDepthBids[row]}
                            </TableCell>
                          </TableRow>
                        );
                      })}
                    </TableBody>
                  </Table>
                </Paper>
              </div>
            </div>
          </Grid>
        </Grid>

        <div>
          if(webSocketRenderingStarted)
          {
            <SockJsClient
              url="http://localhost:8080/orderBook-ws"
              topics={["/topic/all"]}
              onMessage={msg => {
                if (msg.symbol === selectedSymbol.symbol) {
                  console.log(msg);
                  this.setState({
                    ...selectedSymbol,
                    marketDepthAsks: msg.marketDepthForAsks,
                    marketDepthBids: msg.marketDepthForBids
                  });
                }
              }}
              onConnect={() => {
                console.log("connected");
              }}
              ref={client => {
                this.clientRef = client;
              }}
            />
          }
        </div>
      </div>
    );
  }
}

const AppWithStyle = withStyles(styles)(App);

export default AppWithStyle;
