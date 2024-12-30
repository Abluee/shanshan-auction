export enum AuctionStatus {
  NOT_STARTED = 'NOT_STARTED',
  ONGOING = 'ONGOING',
  ENDED = 'ENDED'
}

// 定义状态优先级
const statusPriority = {
  [AuctionStatus.ONGOING]: 1,
  [AuctionStatus.NOT_STARTED]: 2,
  [AuctionStatus.ENDED]: 3
}

export interface AuctionItem {
  id: string;
  title: string;
  description: string;
  startPrice: number;
  currentPrice: number;
  incrementAmount: number;
  startTime: string;
  endTime: string;
  status: AuctionStatus;
  images: string[];
  createdBy: string;
  createdAt: string;
  updatedAt: string;
}

export interface User {
  id: string;
  name: string;
  avatar: string;
}

export interface Bid {
  id: string;
  itemId: string;
  userId: string;
  username: string;
  userAvatar?: string;
  price: number;
  createdAt: string;
}

// 添加排序函数
export function sortAuctionItems(items: AuctionItem[]): AuctionItem[] {
  return [...items].sort((a, b) => {
    // 首先按状态优先级排序
    const statusDiff = statusPriority[a.status] - statusPriority[b.status]
    if (statusDiff !== 0) return statusDiff

    // 如果状态相同，按照以下规则排序：
    switch (a.status) {
      case AuctionStatus.ONGOING:
        // 进行中的按剩余时间升序（越快结束越靠前）
        return new Date(a.endTime).getTime() - new Date(b.endTime).getTime()
      case AuctionStatus.NOT_STARTED:
        // 未开始的按开始时间升序（越快开始越靠前）
        return new Date(a.startTime).getTime() - new Date(b.startTime).getTime()
      case AuctionStatus.ENDED:
        // 已结束的按结束时间降序（最近结束的靠前）
        return new Date(b.endTime).getTime() - new Date(a.endTime).getTime()
      default:
        return 0
    }
  })
}
