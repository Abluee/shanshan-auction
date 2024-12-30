export enum AuctionStatus {
  NOT_STARTED = 'NOT_STARTED',
  ONGOING = 'ONGOING',
  ENDED = 'ENDED'
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
  delayDuration: number;
  status: AuctionStatus;
  createdBy: string;
  createdByName: string;
  images: string[];
  bidHistory: Bid[];
  createdAt: string;
}

export interface User {
  id: string;
  name: string;
  avatar: string;
}

export interface Bid {
  id: string;
  userId: string;
  username: string;
  userAvatar: string | null;
  price: number;
  createdAt: string;
}
