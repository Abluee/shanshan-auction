import {defineStore} from 'pinia'
import type {AuctionItem} from '../types/auction'
import { AuctionStatus } from '../types/auction'
import request from '@/utils/request'

export const useAuctionStore = defineStore('auction', {
  state: () => ({
    auctionItems: [] as AuctionItem[],
    loading: false
  }),

  actions: {
    async fetchAuctions() {
      try {
        const response = await request.get('/items/list')
        this.auctionItems = this.transformItems(response)
      } catch (error) {
        console.error('Failed to fetch auctions:', error)
      }
    },

    async fetchItemById(id: string) {
      try {
        const response = await request.get(`/items/${id}`)
        this.currentItem = this.transformItem(response)
        return this.currentItem
      } catch (error) {
        console.error('Failed to fetch item:', error)
        throw error
      }
    },

    async placeBid(itemId: string, price: number) {
      try {
        await request.post(`/bids/place`, {
          itemId,
          price
        })
        // 重新获取商品详情
        if (this.currentItem?.id === itemId) {
          await this.fetchItemById(itemId)
        }
        await this.fetchAuctions()
      } catch (error) {
        console.error('Failed to place bid:', error)
        throw error
      }
    },

    // 内部辅助方法（去掉 private 修饰符）
    transformItems(items: any[]): AuctionItem[] {
      return items.map(item => this.transformItem(item))
    },

    transformItem(item: any): AuctionItem {
      return {
        id: item.id.toString(),
        title: item.title,
        description: item.description,
        images: item.images,
        startPrice: item.startPrice,
        currentPrice: item.currentPrice,
        startTime: item.startTime,
        endTime: item.endTime,
        incrementAmount: item.incrementAmount,
        delayDuration: item.delayDuration,
        status: item.status,
        bidHistory: item.bidHistory.map((bid: any) => ({
          id: bid.id.toString(),
          userId: bid.userId.toString(),
          price: bid.price,
          time: bid.createdAt,
          username: bid.username,
          userAvatar: bid.userAvatar
        }))
      }
    },

    calculateStatus(item: AuctionItem): AuctionStatus {
      const now = new Date()
      const startTime = new Date(item.startTime)
      const endTime = new Date(item.endTime)

      if (now < startTime) {
        return AuctionStatus.NOT_STARTED
      } else if (now > endTime) {
        return AuctionStatus.ENDED
      } else {
        return AuctionStatus.ONGOING
      }
    }
  }
})
