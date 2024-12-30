export interface AuctionItem {
  id: string;
  title: string;
  description?: string;
  images: string[];
  startPrice: number;
  currentPrice: number;
  startTime: string;
  endTime: string;
  incrementAmount: number;
  delayDuration: number;
  status: 'NOT_STARTED' | 'ONGOING' | 'ENDED';
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
  username: string;
  userAvatar: string;
}
