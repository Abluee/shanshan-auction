import { defineStore } from 'pinia'
import type { AuctionItem, Bid } from '../types/auction'
import dayjs from 'dayjs'

export const useAuctionStore = defineStore('auction', {
  state: () => ({
    auctionItems: [
      {
        id: '1',
        title: 'Nike K 板鞋白绿 42码',
        images: ['https://picsum.photos/800/800?random=1'],
        startPrice: 221,
        currentPrice: 289,
        startTime: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().add(4, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        incrementAmount: 10,
        delayDuration: 5,
        status: 'ongoing',
        bidHistory: [
          {
            id: '1',
            userId: 'user2',
            price: 289,
            time: dayjs().subtract(10, 'minute').format('YYYY-MM-DD HH:mm:ss')
          },
          {
            id: '2',
            userId: 'user3',
            price: 279,
            time: dayjs().subtract(20, 'minute').format('YYYY-MM-DD HH:mm:ss')
          }
        ]
      },
      {
        id: '2',
        title: '[微瑕有盒用过] Age20s气垫',
        images: ['https://picsum.photos/800/800?random=2'],
        startPrice: 41,
        currentPrice: 41,
        startTime: dayjs().add(2, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        incrementAmount: 5,
        delayDuration: 5,
        status: 'notStarted',
        bidHistory: []
      },
      {
        id: '3',
        title: 'VANS黑白Old Skool',
        images: ['https://picsum.photos/800/800?random=3'],
        startPrice: 106,
        currentPrice: 198,
        startTime: dayjs().subtract(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().subtract(1, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        incrementAmount: 8,
        delayDuration: 5,
        status: 'ended',
        bidHistory: [
          {
            id: '3',
            userId: 'user4',
            price: 198,
            time: dayjs().subtract(2, 'hour').format('YYYY-MM-DD HH:mm:ss')
          },
          {
            id: '4',
            userId: 'user2',
            price: 190,
            time: dayjs().subtract(3, 'hour').format('YYYY-MM-DD HH:mm:ss')
          },
          {
            id: '5',
            userId: 'user3',
            price: 182,
            time: dayjs().subtract(4, 'hour').format('YYYY-MM-DD HH:mm:ss')
          }
        ]
      },
      {
        id: '4',
        title: '燕京啤酒9度330毫升6罐',
        images: ['https://picsum.photos/800/800?random=4'],
        startPrice: 15,
        currentPrice: 25,
        startTime: dayjs().subtract(5, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().add(2, 'hour').format('YYYY-MM-DD HH:mm:ss'),
        incrementAmount: 2,
        delayDuration: 5,
        status: 'ongoing',
        bidHistory: [
          {
            id: '6',
            userId: 'user2',
            price: 25,
            time: dayjs().subtract(30, 'minute').format('YYYY-MM-DD HH:mm:ss')
          }
        ]
      },
      {
        id: '5',
        title: '小米手环8 Pro',
        images: ['https://picsum.photos/800/800?random=5'],
        startPrice: 299,
        currentPrice: 299,
        startTime: dayjs().add(1, 'day').format('YYYY-MM-DD HH:mm:ss'),
        endTime: dayjs().add(2, 'day').format('YYYY-MM-DD HH:mm:ss'),
        incrementAmount: 10,
        delayDuration: 5,
        status: 'notStarted',
        bidHistory: []
      }
    ] as AuctionItem[]
  }),

  getters: {
    getAuctionById: (state) => {
      return (id: string) => state.auctionItems.find(item => item.id === id)
    },
    
    getAuctionStatus: () => {
      return (auction: AuctionItem) => {
        const now = dayjs()
        const startTime = dayjs(auction.startTime)
        const endTime = dayjs(auction.endTime)

        if (now.isBefore(startTime)) {
          return 'notStarted'
        } else if (now.isAfter(endTime)) {
          return 'ended'
        } else {
          return 'ongoing'
        }
      }
    }
  },

  actions: {
    addAuction(auction: AuctionItem) {
      this.auctionItems.push({
        ...auction,
        status: this.getAuctionStatus(auction)
      })
    },

    updateAuctionStatus(id: string) {
      const auction = this.auctionItems.find(item => item.id === id)
      if (auction) {
        auction.status = this.getAuctionStatus(auction)
      }
    },

    placeBid(auctionId: string, bid: Bid) {
      const auction = this.auctionItems.find(item => item.id === auctionId)
      if (auction) {
        auction.currentPrice = bid.price
        auction.bidHistory.unshift(bid)

        // 如果在结束前5分钟内有人出价，延长结束时间
        const now = dayjs()
        const endTime = dayjs(auction.endTime)
        const timeToEnd = endTime.diff(now, 'minute')

        if (timeToEnd <= 5) {
          auction.endTime = dayjs().add(auction.delayDuration, 'minute').format('YYYY-MM-DD HH:mm:ss')
        }
      }
    },

    updateAllAuctionStatus() {
      this.auctionItems.forEach(auction => {
        auction.status = this.getAuctionStatus(auction)
      })
    }
  }
}) 