export interface AuctionItem {
  id: string;
  title: string;
  images: string[];
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  incrementAmount: number;
  delayDuration: number; // 延时时长(分钟)
  status: 'notStarted' | 'ongoing' | 'ended';
  lastBidUser?: User;
  bidHistory: Bid[];
}

export interface User {
  id: string;
  name: string;
  avatar: string;
}

export interface Bid {
  id: string;
  userId: string;
  price: number;
  time: string;
} 